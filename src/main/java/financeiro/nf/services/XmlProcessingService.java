package financeiro.nf.services;

import financeiro.nf.models.NfXmlEntity;
import financeiro.nf.repositories.NfXmlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.core.internal.shaded.fdp.v2_19_2.JavaBigDecimalParser.parseBigDecimal;
import static org.apache.tomcat.util.http.FastHttpDateFormat.parseDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class XmlProcessingService {

    private final NfXmlRepository repository;

    @Transactional
    public void processXml(InputStream inputStream, String filename) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            // Extrai dados do XML
            NfXmlEntity nfEntity = extractDataFromXml(doc, filename);

            // Verifica se já existe
            if (repository.existsByNrNotaFiscalAndCdSerieNf(
                    nfEntity.getNrNotaFiscal(), nfEntity.getCdSerieNf())){
                throw new IOException("Nota fiscal já processada: " +
                        nfEntity.getNrNotaFiscal() + " Série: " + nfEntity.getCdSerieNf());
            }

            // Salva no Banco
            repository.save(nfEntity);
            log.info("Nota fiscal {} processada com sucesso", nfEntity.getNrNotaFiscal());

        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Erro ao parsear XML " + filename, e);
        } catch (DataIntegrityViolationException e) {
            throw new IOException("Erro de integridade nos dados do arquivo " + filename, e);
        } catch (Exception e) {
            throw new IOException("Erro ao processar arquivo " + filename, e);
        }
    }

    private NfXmlEntity extractDataFromXml(Document doc, String filename) {
        NfXmlEntity entity = new NfXmlEntity();

        // Tenta primeiro o formato NFS-e (novo)
        NodeList notaNodes = doc.getElementsByTagName("NOTA");
        if (notaNodes.getLength() > 0) {
            // Formato NFS-e encontrado
            extractDataFromNfseFormat((Element) notaNodes.item(0), entity, filename, doc);
        }
        // Tenta o formato NFe anterior
        else if (doc.getElementsByTagNameNS("NFe", "Reg20Item").getLength() > 0) {
            NodeList reg20Items = doc.getElementsByTagNameNS("NFe", "Reg20Item");
            extractDataFromNfeFormat((Element) reg20Items.item(0), entity, filename, doc);
        }
        else {
            // Formato não reconhecido, usa valores padrão
            setDefaultRequiredValues(entity, filename, doc);
            log.warn("⚠️ Formato de XML não reconhecido, usando valores padrão");
        }

        return entity;
    }

    // NOVO MÉTODO: Processa formato NFS-e (novo)
    private void extractDataFromNfseFormat(Element notaElement, NfXmlEntity entity, String filename, Document doc) {
        log.info("📋 Processando XML no formato NFS-e");

        // Dados básicos da nota
        entity.setNrNotaFiscal(getElementText(notaElement, "NUMERO"));
        entity.setCdSerieNf(getElementText(notaElement, "SERIE"));
        entity.setCdCgcEmitente(cleanCnpj(getElementText(notaElement, "CNPJ")));
        entity.setCdCgc(cleanCnpj(getElementText(notaElement, "TOM_CPF_CNPJ")));

        // Datas
        String dtCompetencia = getElementText(notaElement, "DT_COMPETENCIA");
        entity.setDtEmissao(parseDateTime(dtCompetencia));

        // Valores
        String vlServico = getElementText(notaElement, "VL_SERVICO");
        String vlLiquidoNfse = getElementText(notaElement, "VL_LIQUIDO_NFSE");
        BigDecimal vlTotal = parseBigDecimal(vlServico);
        BigDecimal vlLiquido = parseBigDecimal(vlLiquidoNfse);

        entity.setVlTotalNota(vlTotal);

        // CAMPOS OBRIGATÓRIOS - VALORES PADRÃO
        setDefaultRequiredValues(entity, filename, doc);

        // CAMPOS DE ITENS
        entity.setNrItemNf(1L);
        entity.setQtItemNf(BigDecimal.ONE);
        entity.setVlUnitarioItemNf(vlTotal);
        entity.setVlTotalItemNf(vlTotal);
        entity.setVlDesconto(BigDecimal.ZERO);
        entity.setVlLiquido(vlLiquido);

        // Outros campos
        entity.setVlMercadoria(vlTotal);
        entity.setVlDescontos(parseBigDecimal(getElementText(notaElement, "VL_DESCONTO_INCONDICIONADO")));
        entity.setVlFrete(BigDecimal.ZERO);
        entity.setVlSeguro(BigDecimal.ZERO);
        entity.setVlDespesaAcessoria(BigDecimal.ZERO);
        entity.setVlIpi(BigDecimal.ZERO);
        entity.setNrSequenciaNf(0L);
        entity.setCdEstabelecimento(1L);
        entity.setCdOperacaoNf(1L);

        // Observação com dados adicionais
        String discriminacao = getElementText(notaElement, "DISCRIMINACAO");
        if (discriminacao != null && discriminacao.length() > 255) {
            discriminacao = discriminacao.substring(0, 255);
        }
        entity.setDsObservacao(discriminacao);

        String razaoSocialEmitente = getElementText(notaElement, "PRE_RAZAO_SOCIAL");
        String razaoSocialTomador = getElementText(notaElement, "TOM_RAZAO_SOCIAL");

        log.info("✅ NFS-e {} processada - Emitente: {}, Tomador: {}",
                entity.getNrNotaFiscal(), razaoSocialEmitente, razaoSocialTomador);
    }

    // MÉTODO EXISTENTE: Processa formato NFe (antigo) - apenas renomeado
    private void extractDataFromNfeFormat(Element reg20Item, NfXmlEntity entity, String filename, Document doc) {
        log.info("📋 Processando XML no formato NFe");

        // Mapeamento dos campos do XML
        entity.setNrNotaFiscal(getElementText(reg20Item, "NumNf"));
        entity.setCdSerieNf(getElementText(reg20Item, "SerNf"));
        entity.setCdCgcEmitente(cleanCnpj(getElementText(reg20Item, "CpfCnpjPre")));
        entity.setCdCgc(cleanCnpj(getElementText(reg20Item, "CpfCnpjTom")));

        // Datas
        String dtEmiNf = getElementText(reg20Item, "DtEmiNf");
        entity.setDtEmissao(parseDateTime(dtEmiNf));

        // Valores
        String vlNFS = getElementText(reg20Item, "VlNFS");
        BigDecimal vlTotal = parseBigDecimal(vlNFS);
        entity.setVlTotalNota(vlTotal);

        // CAMPOS OBRIGATÓRIOS - VALORES PADRÃO
        setDefaultRequiredValues(entity, filename, doc);

        // CAMPOS DE ITENS
        entity.setNrItemNf(1L);
        entity.setQtItemNf(BigDecimal.ONE);
        entity.setVlUnitarioItemNf(vlTotal);
        entity.setVlTotalItemNf(vlTotal);
        entity.setVlDesconto(BigDecimal.ZERO);
        entity.setVlLiquido(vlTotal);

        // Outros campos que podem ser obrigatórios
        entity.setVlMercadoria(vlTotal);
        entity.setVlDescontos(BigDecimal.ZERO);
        entity.setVlFrete(BigDecimal.ZERO);
        entity.setVlSeguro(BigDecimal.ZERO);
        entity.setVlDespesaAcessoria(BigDecimal.ZERO);
        entity.setVlIpi(BigDecimal.ZERO);
        entity.setNrSequenciaNf(0L);
        entity.setCdEstabelecimento(1L);
        entity.setCdOperacaoNf(1L);

        log.info("✅ NFe {} processada com sucesso", entity.getNrNotaFiscal());
    }

    // ATUALIZADO: Agora busca em ambos os formatos (com e sem namespace)
    private String getElementText(Element parent, String tagName) {
        // Tenta primeiro sem namespace (para NFS-e)
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }

        // Tenta com namespace (para NFe antigo)
        nodes = parent.getElementsByTagNameNS("NFe", tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }

        return null;
    }

    // MÉTODOS AUXILIARES (mantidos iguais)
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            // Tenta formato ISO "yyyy-MM-dd" (do NFS-e)
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
                return date.atStartOfDay();
            }
            // Tenta formato "dd/MM/yyyy" (do NFe antigo)
            else if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
                return date.atStartOfDay();
            }
            else {
                log.warn("⚠️ Formato de data não reconhecido: {}, usando data atual", dateStr);
                return LocalDateTime.now();
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao parsear data: {}, usando data atual", dateStr);
            return LocalDateTime.now();
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String cleanedValue = value.trim()
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(cleanedValue);
        } catch (Exception e) {
            log.warn("⚠️ Não foi possível parsear o valor: {}, usando ZERO", value);
            return BigDecimal.ZERO;
        }
    }

    private String truncateXmlString(String xml, int maxLength) {
        if (xml == null) {
            return "";
        }
        return (xml.length() > maxLength) ? xml.substring(0, maxLength) : xml;
    }

    private void setDefaultRequiredValues(NfXmlEntity entity, String filename, Document doc) {
        entity.setIeAcaoNf("A");
        entity.setIeEmissaoNf("1");
        entity.setIeTipoFrete("9");
        entity.setQtPesoBruto(BigDecimal.ZERO);
        entity.setQtPesoLiquido(BigDecimal.ZERO);
        entity.setDtAtualizacao(LocalDateTime.now());
        entity.setNmUsuario("XML_PROCESSOR");
        entity.setIeNfEletronica("S");
        entity.setDsLinkXml(filename);
        entity.setDsXmlCompl(truncateXmlString(documentToString(doc), 4000));

        // Se não tiver número/série definidos, usa padrão
        if (entity.getNrNotaFiscal() == null) {
            entity.setNrNotaFiscal("000000");
        }
        if (entity.getCdSerieNf() == null) {
            entity.setCdSerieNf("001");
        }
        if (entity.getDtEmissao() == null) {
            entity.setDtEmissao(LocalDateTime.now());
        }
        if (entity.getVlTotalNota() == null) {
            entity.setVlTotalNota(BigDecimal.ZERO);
        }
    }

    private String cleanCnpj(String cnpj) {
        if (cnpj == null) return null;
        return cnpj.replaceAll("[^0-9]", "");
    }

    private String documentToString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            log.error("Erro ao converter XML para string", e);
            return "";
        }
    }
}
/*public class XmlProcessingService {

    private final NfXmlRepository repository;

    @Transactional
    public void processXml(InputStream inputStream, String filename) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            // Extrai dados do XML
            NfXmlEntity nfEntity = extractDataFromXml(doc, filename);

            // Verifica se já existe
            if (repository.existsByNrNotaFiscalAndCdSerieNf(
                nfEntity.getNrNotaFiscal(), nfEntity.getCdSerieNf())){
                throw new IOException("Nota fiscal já processada: " +
                        nfEntity.getNrNotaFiscal() + " Série: " + nfEntity.getCdSerieNf());
            }

            // Salva no Banco
            repository.save(nfEntity);
            log.info("Nota fiscal {} processada com sucesso", nfEntity.getNrNotaFiscal());

        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Erro ao parsear XML " + filename, e);
        } catch (DataIntegrityViolationException e) {
            throw new IOException("Erro de integridade nos dados do arquivo " + filename, e);
        } catch (Exception e) {
            throw new IOException("Erro ao processar  arquivo " + filename, e);
        }
    }

    private NfXmlEntity extractDataFromXml(Document doc, String filename) {
        NfXmlEntity entity = new NfXmlEntity();

        NodeList reg20Items = doc.getElementsByTagNameNS("NFe", "Reg20Item");
        if (reg20Items.getLength() > 0) {
            Element reg20Item = (Element) reg20Items.item(0);

            // Mapeamento dos campos do XML
            entity.setNrNotaFiscal(getElementText(reg20Item, "NumNf"));
            entity.setCdSerieNf(getElementText(reg20Item, "SerNf"));
            entity.setCdCgcEmitente(cleanCnpj(getElementText(reg20Item, "CpfCnpjPre")));
            entity.setCdCgc(cleanCnpj(getElementText(reg20Item, "CpfCnpjTom")));

            // Datas
            String dtEmiNf = getElementText(reg20Item, "DtEmiNf");
            entity.setDtEmissao(parseDateTime(dtEmiNf));

            // Valores
            String vlNFS = getElementText(reg20Item, "VlNFS");
            entity.setVlTotalNota(parseBigDecimal(vlNFS));

            // CAMPOS OBRIGATÓRIOS - VALORES PADRÃO
            entity.setIeAcaoNf("A");                    // Ação da NF (padrão: "A")
            entity.setIeEmissaoNf("1");                 // Emissão própria
            entity.setIeTipoFrete("9");                 // Tipo de frete (padrão: 9 - Sem frete)
            entity.setQtPesoBruto(BigDecimal.ZERO);     // Peso bruto
            entity.setQtPesoLiquido(BigDecimal.ZERO);   // Peso líquido
            entity.setDtAtualizacao(LocalDateTime.now()); // Data atualização
            entity.setNmUsuario("XML_PROCESSOR");       // Usuário do sistema

            // Campos específicos da NFe
            entity.setIeNfEletronica("S");              // NF eletrônica
            entity.setDsLinkXml(filename);              // Nome do arquivo
            entity.setDsXmlCompl(truncateXmlString(documentToString(doc), 4000)); // XML completo

            // Outros campos que podem ser obrigatórios
            entity.setVlMercadoria(parseBigDecimal(vlNFS)); // Valor da mercadoria = valor total
            entity.setVlDescontos(BigDecimal.ZERO);     // Descontos
            entity.setVlFrete(BigDecimal.ZERO);         // Frete
            entity.setVlSeguro(BigDecimal.ZERO);        // Seguro
            entity.setVlDespesaAcessoria(BigDecimal.ZERO); // Despesas acessórias
            entity.setVlIpi(BigDecimal.ZERO);           // IPI

            // Se houver outros campos NOT NULL, adicione aqui
            entity.setNrSequenciaNf(0L);                // Número sequencial NF
            entity.setCdEstabelecimento(1L);            // Estabelecimento padrão
            entity.setCdOperacaoNf(1L);                 // Operação padrão

        } else {
            // Se não encontrar Reg20Item, define valores mínimos obrigatórios
            setDefaultRequiredValues(entity, filename);
            log.warn("⚠️ Nó Reg20Item não encontrado no XML, usando valores padrão");
        }

        return entity;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now(); // Retorna data atual se não tiver data
        }

        try {
            // Tenta parse no formato "dd/MM/yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
            return date.atStartOfDay();
        } catch (Exception e1) {
            try {
                // Tenta parse no formato ISO "yyyy-MM-dd"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(dateStr.trim(), formatter);
                return date.atStartOfDay();
            } catch (Exception e2) {
                log.warn("⚠️ Não foi possível parsear a data: {}, usando data atual", dateStr);
                return LocalDateTime.now(); // Fallback para data atual
            }
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // Remove possíveis separadores de milhares e formata para decimal
            String cleanedValue = value.trim()
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(cleanedValue);
        } catch (Exception e) {
            log.warn("⚠️ Não foi possível parsear o valor: {}, usando ZERO", value);
            return BigDecimal.ZERO;
        }
    }

    private String truncateXmlString(String xml, int maxLength) {
        if (xml == null) {
            return "";
        }
        return (xml.length() > maxLength) ? xml.substring(0, maxLength) : xml;
    }

    private void setDefaultRequiredValues(NfXmlEntity entity, String filename) {
        entity.setIeAcaoNf("A");
        entity.setIeEmissaoNf("1");
        entity.setIeTipoFrete("9");
        entity.setQtPesoBruto(BigDecimal.ZERO);
        entity.setQtPesoLiquido(BigDecimal.ZERO);
        entity.setDtAtualizacao(LocalDateTime.now());
        entity.setNmUsuario("XML_PROCESSOR");
        entity.setIeNfEletronica("S");
        entity.setDsLinkXml(filename);
        entity.setNrNotaFiscal("000000"); // Número padrão
        entity.setCdSerieNf("001");       // Série padrão
        entity.setDtEmissao(LocalDateTime.now()); // Data emissão = atual
        entity.setVlTotalNota(BigDecimal.ZERO); // Valor total zero
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("NFe", tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private String cleanCnpj(String cnpj) {
        if (cnpj == null) return null;
        return cnpj.replaceAll("[^0-9]", "");
    }

    private LocalDateTime parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return date.atStartOfDay();
        } catch (Exception e) {
            log.warn("Erro ao parsear data: {}", dateStr);
            return null;
        }
    }
    private String documentToString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            log.error("Erro ao converter XML para string", e);
            return "";
        }
    }


}*/
