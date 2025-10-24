package financeiro.nf.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "NF_NFITEM_NFANEXO_XMLFS", schema = "WEBSERVICE")
public class NfXmlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NR_SEQUENCIA")
    private Long nrSequencia;

    @Column(name = "CD_ESTABELECIMENTO")
    private Long cdEstabelecimento;

    @Column(name = "CD_CGC_EMITENTE", length = 14)
    private String cdCgcEmitente;

    @Column(name = "CD_SERIE_NF", length = 255)
    private String cdSerieNf;

    @Column(name = "NR_SEQUENCIA_NF")
    private Long nrSequenciaNf;

    @Column(name = "CD_OPERACAO_NF")
    private Long cdOperacaoNf;

    @Column(name = "DT_EMISSAO")
    private LocalDateTime dtEmissao;

    @Column(name = "DT_ENTRADA_SAIDA")
    private LocalDateTime dtEntradaSaida;

    @Column(name = "IE_ACAO_NF", length = 1)
    private String ieAcaoNf;

    @Column(name = "IE_EMISSAO_NF", length = 1)
    private String ieEmissaoNf;

    @Column(name = "IE_TIPO_FRETE", length = 1)
    private String ieTipoFrete;

    @Column(name = "VL_MERCADORIA")
    private BigDecimal vlMercadoria;

    @Column(name = "VL_TOTAL_NOTA")
    private BigDecimal vlTotalNota;

    @Column(name = "QT_PESO_BRUTO")
    private BigDecimal qtPesoBruto;

    @Column(name = "QT_PESO_LIQUIDO")
    private BigDecimal qtPesoLiquido;

    @Column(name = "DT_ATUALIZACAO")
    private LocalDateTime dtAtualizacao;

    @Column(name = "NM_USUARIO", length = 15)
    private String nmUsuario;

    @Column(name = "CD_CONDICAO_PAGAMENTO")
    private Long cdCondicaoPagamento;

    @Column(name = "DT_CONTABIL")
    private LocalDateTime dtContabil;

    @Column(name = "CD_CGC", length = 14)
    private String cdCgc;

    @Column(name = "CD_PESSOA_FISICA", length = 10)
    private String cdPessoaFisica;

    @Column(name = "VL_IPI")
    private BigDecimal vlIpi;

    @Column(name = "VL_DESCONTOS")
    private BigDecimal vlDescontos;

    @Column(name = "VL_FRETE")
    private BigDecimal vlFrete;

    @Column(name = "VL_SEGURO")
    private BigDecimal vlSeguro;

    @Column(name = "VL_DESPESA_ACESSORIA")
    private BigDecimal vlDespesaAcessoria;

    @Column(name = "DS_OBSERVACAO", length = 4000)
    private String dsObservacao;

    @Column(name = "NR_NOTA_FISCAL", length = 255)
    private String nrNotaFiscal;

    @Column(name = "NR_DANFE", length = 60)
    private String nrDanfe;

    @Column(name = "IE_NF_ELETRONICA", length = 1)
    private String ieNfEletronica;

    @Column(name = "DS_XML_COMPL", length = 4000)
    private String dsXmlCompl;

    @Column(name = "DS_LINK_XML", length = 255)
    private String dsLinkXml;

    @Column(name = "DT_TRANSMISSAO_NFE")
    private LocalDateTime dtTransmissaoNfe;

    @Column(name = "DT_CANCELAMENTO")
    private LocalDateTime dtCancelamento;

    @Column(name = "DS_MOTIVO_CANCEL_NFE", length = 4000)
    private String dsMotivoCancelNfe;

    @Column(name = "NR_ITEM_NF", nullable = false)
    private Long nrItemNf = 1L;                     // Número do item (fixo: 1)

    @Column(name = "QT_ITEM_NF", nullable = false)
    private BigDecimal qtItemNf = BigDecimal.ONE;   // Quantidade (fixo: 1)

    @Column(name = "VL_UNITARIO_ITEM_NF", nullable = false)
    private BigDecimal vlUnitarioItemNf = BigDecimal.ZERO; // Valor unitário

    @Column(name = "VL_TOTAL_ITEM_NF", nullable = false)
    private BigDecimal vlTotalItemNf = BigDecimal.ZERO;    // Valor total item

    @Column(name = "VL_DESCONTO", nullable = false)
    private BigDecimal vlDesconto = BigDecimal.ZERO;       // Desconto

    @Column(name = "VL_LIQUIDO", nullable = false)
    private BigDecimal vlLiquido = BigDecimal.ZERO;        // Valor líquido

    public Long getNrSequencia() {
        return nrSequencia;
    }

    public void setNrSequencia(Long nrSequencia) {
        this.nrSequencia = nrSequencia;
    }

    public Long getCdEstabelecimento() {
        return cdEstabelecimento;
    }

    public void setCdEstabelecimento(Long cdEstabelecimento) {
        this.cdEstabelecimento = cdEstabelecimento;
    }

    public String getCdCgcEmitente() {
        return cdCgcEmitente;
    }

    public void setCdCgcEmitente(String cdCgcEmitente) {
        this.cdCgcEmitente = cdCgcEmitente;
    }

    public String getCdSerieNf() {
        return cdSerieNf;
    }

    public void setCdSerieNf(String cdSerieNf) {
        this.cdSerieNf = cdSerieNf;
    }

    public Long getNrSequenciaNf() {
        return nrSequenciaNf;
    }

    public void setNrSequenciaNf(Long nrSequenciaNf) {
        this.nrSequenciaNf = nrSequenciaNf;
    }

    public Long getCdOperacaoNf() {
        return cdOperacaoNf;
    }

    public void setCdOperacaoNf(Long cdOperacaoNf) {
        this.cdOperacaoNf = cdOperacaoNf;
    }

    public LocalDateTime getDtEmissao() {
        return dtEmissao;
    }

    public void setDtEmissao(LocalDateTime dtEmissao) {
        this.dtEmissao = dtEmissao;
    }

    public LocalDateTime getDtEntradaSaida() {
        return dtEntradaSaida;
    }

    public void setDtEntradaSaida(LocalDateTime dtEntradaSaida) {
        this.dtEntradaSaida = dtEntradaSaida;
    }

    public String getIeAcaoNf() {
        return ieAcaoNf;
    }

    public void setIeAcaoNf(String ieAcaoNf) {
        this.ieAcaoNf = ieAcaoNf;
    }

    public String getIeEmissaoNf() {
        return ieEmissaoNf;
    }

    public void setIeEmissaoNf(String ieEmissaoNf) {
        this.ieEmissaoNf = ieEmissaoNf;
    }

    public String getIeTipoFrete() {
        return ieTipoFrete;
    }

    public void setIeTipoFrete(String ieTipoFrete) {
        this.ieTipoFrete = ieTipoFrete;
    }

    public BigDecimal getVlMercadoria() {
        return vlMercadoria;
    }

    public void setVlMercadoria(BigDecimal vlMercadoria) {
        this.vlMercadoria = vlMercadoria;
    }

    public BigDecimal getVlTotalNota() {
        return vlTotalNota;
    }

    public void setVlTotalNota(BigDecimal vlTotalNota) {
        this.vlTotalNota = vlTotalNota;
    }

    public BigDecimal getQtPesoBruto() {
        return qtPesoBruto;
    }

    public void setQtPesoBruto(BigDecimal qtPesoBruto) {
        this.qtPesoBruto = qtPesoBruto;
    }

    public BigDecimal getQtPesoLiquido() {
        return qtPesoLiquido;
    }

    public void setQtPesoLiquido(BigDecimal qtPesoLiquido) {
        this.qtPesoLiquido = qtPesoLiquido;
    }

    public LocalDateTime getDtAtualizacao() {
        return dtAtualizacao;
    }

    public void setDtAtualizacao(LocalDateTime dtAtualizacao) {
        this.dtAtualizacao = dtAtualizacao;
    }

    public String getNmUsuario() {
        return nmUsuario;
    }

    public void setNmUsuario(String nmUsuario) {
        this.nmUsuario = nmUsuario;
    }

    public Long getCdCondicaoPagamento() {
        return cdCondicaoPagamento;
    }

    public void setCdCondicaoPagamento(Long cdCondicaoPagamento) {
        this.cdCondicaoPagamento = cdCondicaoPagamento;
    }

    public LocalDateTime getDtContabil() {
        return dtContabil;
    }

    public void setDtContabil(LocalDateTime dtContabil) {
        this.dtContabil = dtContabil;
    }

    public String getCdCgc() {
        return cdCgc;
    }

    public void setCdCgc(String cdCgc) {
        this.cdCgc = cdCgc;
    }

    public String getCdPessoaFisica() {
        return cdPessoaFisica;
    }

    public void setCdPessoaFisica(String cdPessoaFisica) {
        this.cdPessoaFisica = cdPessoaFisica;
    }

    public BigDecimal getVlIpi() {
        return vlIpi;
    }

    public void setVlIpi(BigDecimal vlIpi) {
        this.vlIpi = vlIpi;
    }

    public BigDecimal getVlDescontos() {
        return vlDescontos;
    }

    public void setVlDescontos(BigDecimal vlDescontos) {
        this.vlDescontos = vlDescontos;
    }

    public BigDecimal getVlFrete() {
        return vlFrete;
    }

    public void setVlFrete(BigDecimal vlFrete) {
        this.vlFrete = vlFrete;
    }

    public BigDecimal getVlSeguro() {
        return vlSeguro;
    }

    public void setVlSeguro(BigDecimal vlSeguro) {
        this.vlSeguro = vlSeguro;
    }

    public BigDecimal getVlDespesaAcessoria() {
        return vlDespesaAcessoria;
    }

    public void setVlDespesaAcessoria(BigDecimal vlDespesaAcessoria) {
        this.vlDespesaAcessoria = vlDespesaAcessoria;
    }

    public String getDsObservacao() {
        return dsObservacao;
    }

    public void setDsObservacao(String dsObservacao) {
        this.dsObservacao = dsObservacao;
    }

    public String getNrNotaFiscal() {
        return nrNotaFiscal;
    }

    public void setNrNotaFiscal(String nrNotaFiscal) {
        this.nrNotaFiscal = nrNotaFiscal;
    }

    public String getNrDanfe() {
        return nrDanfe;
    }

    public void setNrDanfe(String nrDanfe) {
        this.nrDanfe = nrDanfe;
    }

    public String getIeNfEletronica() {
        return ieNfEletronica;
    }

    public void setIeNfEletronica(String ieNfEletronica) {
        this.ieNfEletronica = ieNfEletronica;
    }

    public String getDsXmlCompl() {
        return dsXmlCompl;
    }

    public void setDsXmlCompl(String dsXmlCompl) {
        this.dsXmlCompl = dsXmlCompl;
    }

    public String getDsLinkXml() {
        return dsLinkXml;
    }

    public void setDsLinkXml(String dsLinkXml) {
        this.dsLinkXml = dsLinkXml;
    }

    public LocalDateTime getDtTransmissaoNfe() {
        return dtTransmissaoNfe;
    }

    public void setDtTransmissaoNfe(LocalDateTime dtTransmissaoNfe) {
        this.dtTransmissaoNfe = dtTransmissaoNfe;
    }

    public LocalDateTime getDtCancelamento() {
        return dtCancelamento;
    }

    public void setDtCancelamento(LocalDateTime dtCancelamento) {
        this.dtCancelamento = dtCancelamento;
    }

    public String getDsMotivoCancelNfe() {
        return dsMotivoCancelNfe;
    }

    public void setDsMotivoCancelNfe(String dsMotivoCancelNfe) {
        this.dsMotivoCancelNfe = dsMotivoCancelNfe;
    }

    public Long getNrItemNf() {
        return nrItemNf;
    }

    public void setNrItemNf(Long nrItemNf) {
        this.nrItemNf = nrItemNf;
    }

    public BigDecimal getQtItemNf() {
        return qtItemNf;
    }

    public void setQtItemNf(BigDecimal qtItemNf) {
        this.qtItemNf = qtItemNf;
    }

    public BigDecimal getVlUnitarioItemNf() {
        return vlUnitarioItemNf;
    }

    public void setVlUnitarioItemNf(BigDecimal vlUnitarioItemNf) {
        this.vlUnitarioItemNf = vlUnitarioItemNf;
    }

    public BigDecimal getVlTotalItemNf() {
        return vlTotalItemNf;
    }

    public void setVlTotalItemNf(BigDecimal vlTotalItemNf) {
        this.vlTotalItemNf = vlTotalItemNf;
    }

    public BigDecimal getVlDesconto() {
        return vlDesconto;
    }

    public void setVlDesconto(BigDecimal vlDesconto) {
        this.vlDesconto = vlDesconto;
    }

    public BigDecimal getVlLiquido() {
        return vlLiquido;
    }

    public void setVlLiquido(BigDecimal vlLiquido) {
        this.vlLiquido = vlLiquido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NfXmlEntity that)) return false;
        return Objects.equals(getNrSequencia(), that.getNrSequencia());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNrSequencia());
    }
}
