package financeiro.nf.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class NfeProcessingService {
    private static final Logger log = LoggerFactory.getLogger(NfeProcessingService.class);

    private final FtpService ftpService;
    private final XmlProcessingService xmlProcessingService;

    public NfeProcessingService(FtpService ftpService, XmlProcessingService xmlProcessingService) {
        this.ftpService = ftpService;
        this.xmlProcessingService = xmlProcessingService;
    }

    @Value("${file.processing.enabled:true}")
    private boolean processingEnable;

    @Value("${ftp.xmls-dir:/nfs/NFe}")
    private String xmlsDir;

    @Scheduled(fixedDelayString = "${file.processing.delay:60000}")
    public void processXmlFiles() {
        log.info("🚀 MÉTODO AGENDADO EXECUTADO - Verificando XMLs...");

        if (!processingEnable) {
            log.warn("❌ Processamento de XMLs está DESABILITADO pela configuração");
            return;
        }

        log.info("📁 Iniciando verificação de XMLs no diretório: {}", xmlsDir);

        try{
            List<String> files = ftpService.listFiles(xmlsDir);
            log.info("📊 Total de arquivos encontrados: {}", files.size());

            if (files.isEmpty()) {
                log.info("📭 Nenhum arquivo XML encontrado no diretório: {}", xmlsDir);
                return;
            }

            log.info("📄 Arquivos encontrados: {}", files);

            files.forEach(file -> {
                String sourcePath = xmlsDir + "/" + file;
                String targetPath = xmlsDir + "/processed/" + file;
                log.info("🔄 Processando arquivo: {}", file);
                processXmlFile(sourcePath, targetPath, file);
            });

        } catch (IOException e) {
            log.error("💥 Falha ao verificar XMLs no FTP", e);
        }

        log.info("✅ Verificação de XMLs concluída");
    }

    private void processXmlFile(String sourcePath, String targetPath, String filename) {
        try (InputStream is = ftpService.downloadFile(sourcePath)) {
            xmlProcessingService.processXml(is, filename);

            if (ftpService.moveFile(sourcePath, targetPath)) {
                log.info("XML {} processado e movido com sucesso", filename);
            }else {
                log.error("Falha ao mover XML {}", filename);
            }
        }catch (IOException ioException) {
            log.error("Erro ao mover arquivo falho {} para a pasta de erro", filename, ioException);
        } catch (Exception e) {
            log.error("Erro no processamento do XML {}", filename, e);
            handleFailedXmlFile(sourcePath, filename);
        }
    }

    private void handleFailedXmlFile(String sourcePath, String filename) {
        try {
            String errorPath = sourcePath.replace("/NFe/", "/error/");
            if (ftpService.moveFile(sourcePath, errorPath)) {
                log.warn("XML {} movido para pasta de erro", filename);
            } else {
                log.error("Falha ao mover XML {} para pasta de erro", filename);
            }
        } catch (IOException ioException) {
            log.error("Erro ao mover arquivo falho {} para pasta de erro", filename, ioException);
        }
    }

    @PostConstruct
    public void debugFtpOnStartup() {
        log.info("=== DEBUG INICIAL DO FTP ===");
        try {
            ftpService.debugFtpDirectory("/Ftp/nfs/NFE");

            // Também testa caminhos alternativos
            ftpService.debugFtpDirectory("/Ftp");
            ftpService.debugFtpDirectory("/nfs");

        } catch (IOException e) {
            log.error("Erro no debug FTP", e);
        }
    }

    @PostConstruct
    public void testListFiles() {
        log.info("=== TESTE MANUAL DO listFiles ===");
        try {
            List<String> files = ftpService.listFiles("/nfs");
            log.info("🎯 RESULTADO DO TESTE - Arquivos encontrados: {}", files);
        } catch (IOException e) {
            log.error("Erro no teste", e);
        }
    }
}