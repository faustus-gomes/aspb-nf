package financeiro.nf.services;

import financeiro.nf.config.FtpConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;

@Service
@Slf4j
public class FtpService {

    @Autowired
    private FtpConfig ftpConfig;

    public List<String> listFiles(String directoryPath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        List<String> files = new ArrayList<>();

        try {
            connectFtp(ftpClient);

            log.debug("🔍 Listando arquivos no diretório: {}", directoryPath);

            // Método mais confiável para listar arquivos
            FTPFile[] ftpFiles = ftpClient.listFiles(directoryPath);
            log.debug("📊 Encontrados {} itens no total", ftpFiles.length);

            for (FTPFile file : ftpFiles) {
                String filename = file.getName();
                log.debug("📄 Analisando: {} (Diretório: {}, Tamanho: {})",
                        filename, file.isDirectory(), file.getSize());

                if (file.isFile()) {
                    // Verifica se é XML (case insensitive)
                    if (filename.toLowerCase().endsWith(".xml")) {
                        files.add(filename);
                        log.debug("✅ XML adicionado: {}", filename);
                    } else {
                        log.debug("❌ Ignorado (não é XML): {}", filename);
                    }
                } else {
                    log.debug("📁 Ignorado (é diretório): {}", filename);
                }
            }

            log.info("🎯 Total de XMLs encontrados em {}: {}", directoryPath, files.size());

        } catch (IOException e) {
            log.error("💥 Erro ao listar arquivos no diretório {}", directoryPath, e);
            throw e;
        } finally {
            disconnectFtpClient(ftpClient);
        }

        return files;
    }

    private void disconnectFtpClient(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                log.debug("🔌 Desconectado do FTP");
            }
        } catch (IOException ex) {
            log.error("Erro ao desconectar do FTP", ex);
        }
    }

    public InputStream downloadFile(String filePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        InputStream inputStream = null;

        try {
            // 1. Conecta e autentica
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                throw new IOException("Falha na conexão FTP: " + ftpClient.getReplyString());
            }

            if (!ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword())) {
                throw new IOException("Falha no login FTP: " + ftpClient.getReplyString());
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 2. Obtém o stream com verificação
            inputStream = ftpClient.retrieveFileStream(filePath);
            if (inputStream == null) {
                throw new IOException("Falha ao baixar arquivo. Resposta FTP: " + ftpClient.getReplyString());
            }

            // 3. Cria um wrapper que gerencia a desconexão
            return new FilterInputStream(inputStream) {
                private boolean closed = false;

                @Override
                public void close() throws IOException {
                    if (closed) return;

                    try {
                        super.close();
                    } finally {
                        try {
                            if (!ftpClient.completePendingCommand()) {
                                log.error("Falha ao completar comando FTP: {}", ftpClient.getReplyString());
                            }
                        } finally {
                            if (ftpClient.isConnected()) {
                                ftpClient.disconnect();
                            }
                            closed = true;
                        }
                    }
                }

                @Override
                protected void finalize() throws Throwable {
                    if (!closed) {
                        log.warn("InputStream não foi fechado corretamente!");
                        close();
                    }
                }
            };

        } catch (IOException e) {
            // Limpeza em caso de erro
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    log.error("Erro ao fechar InputStream", ex);
                }
            }
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    log.error("Erro ao desconectar FTP", ex);
                }
            }
            throw new IOException("Erro no download do arquivo: " + filePath, e);
        }
    }

    public boolean moveFile(String sourcePath, String targetPath) throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Verifica se o arquivo de origem existe
            FTPFile[] files = ftpClient.listFiles(sourcePath);
            if (files == null || files.length == 0) {
                log.warn("Arquivo de origem não encontrado: {}", sourcePath);
                return false;
            }

            // Cria diretórios de destino se não existirem
            //createDirectories(ftpClient, Paths.get(targetPath).getParent().toString());


            // Renomeia (move) o arquivo
            boolean success = ftpClient.rename(sourcePath, targetPath);

            if (!success) {
                log.error("Falha ao mover arquivo. Código de retorno: {}", ftpClient.getReplyCode());
                throw new IOException("Falha ao mover arquivo no FTP");
            }

            log.debug("Arquivo movido de {} para {}", sourcePath, targetPath);
            return true;

        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.error("Erro ao desconectar FTP", e);
                }
            }
        }
    }

    public boolean createDirectory(String path) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            return ftpClient.makeDirectory(path);
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        }
    }

    public boolean exists(String filePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            return ftpClient.listFiles(filePath).length > 0;
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        }
    }
    public void debugFtpDirectory(String directoryPath) throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            connectFtp(ftpClient);

            log.info("=== DEBUG FTP DIRECTORY ===");
            log.info("📁 Verificando diretório: {}", directoryPath);

            // Verifica se o diretório existe
            boolean dirExists = ftpClient.changeWorkingDirectory(directoryPath);
            log.info("✅ Diretório existe? {}", dirExists);

            if (dirExists) {
                // Lista TODOS os arquivos do diretório
                FTPFile[] allFiles = ftpClient.listFiles(directoryPath);
                log.info("📊 Total de itens no diretório: {}", allFiles.length);

                for (FTPFile file : allFiles) {
                    if (file.isDirectory()) {
                        log.info("📁 DIR: {} (Tamanho: {})", file.getName(), file.getSize());
                    } else {
                        log.info("📄 FILE: {} (Tamanho: {}, Extensão: {})",
                                file.getName(),
                                file.getSize(),
                                getFileExtension(file.getName()));
                    }
                }
            } else {
                log.error("❌ Diretório NÃO existe: {}", directoryPath);

                // Lista diretório raiz para ajudar no debug
                log.info("=== DIRETÓRIO RAIZ ===");
                FTPFile[] rootFiles = ftpClient.listFiles("/");
                for (FTPFile file : rootFiles) {
                    if (file.isDirectory()) {
                        log.info("📁 {}", file.getName());
                    } else {
                        log.info("📄 {}", file.getName());
                    }
                }
            }

        } finally {
            disconnectFtpClient(ftpClient);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot) : "(sem extensão)";
    }

    private void connectFtp(FTPClient ftpClient) throws IOException {
        try {
            log.debug("🔌 Conectando ao FTP: {}:{}", ftpConfig.getHost(), ftpConfig.getPort());
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new IOException("Falha na conexão FTP. Código: " + replyCode);
            }

            log.debug("🔐 Login no FTP: {}", ftpConfig.getUsername());
            boolean loginSuccess = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            if (!loginSuccess) {
                throw new IOException("Falha no login FTP");
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setControlKeepAliveTimeout(300); // 5 minutos

            log.info("✅ Conectado com sucesso ao FTP: {}", ftpConfig.getHost());

        } catch (IOException e) {
            log.error("❌ Erro na conexão FTP: {}", e.getMessage());
            disconnectFtpClient(ftpClient);
            throw e;
        }
    }
}
