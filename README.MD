📄 NFe Processor - Sistema de Processamento de Notas Fiscais Eletrônicas

Sistema Spring Boot para processamento automático de arquivos XML de Notas Fiscais Eletrônicas (NFE) via FTP e persistência em banco de dados Oracle.

🚀 Funcionalidades

📥 Download automático de arquivos XML de servidor FTP
🔍 Processamento e parsing de dados de NFe
💾 Persistência em banco de dados Oracle
📁 Movimentação automática de arquivos processados
⏰ Agendamento de processamento a cada 60 segundos
❌ Tratamento de erros com movimentação para pasta de erro
🛠️ Tecnologias

Java 19
Spring Boot 3.5.7
Spring Data JPA
Oracle Database
Apache Commons Net (FTP Client)
Lombok
Maven
📋 Pré-requisitos

Java 19 ou superior
Maven 3.6+
Banco Oracle 11g+
Servidor FTP compatível
⚙️ Configuração

1. Clone o projeto
> git clone <url-do-repositorio>
   cd nfe-processor

2. Configuração do Banco de Dados

Edite src/main/resources/application.properties:
>
# Database
>spring.datasource.url=jdbc:oracle:thin:@host:port/SID
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# FTP
>ftp.host=seu_host_ftp
ftp.port=21
ftp.username=usuario_ftp
ftp.password=senha_ftp
ftp.xmls-dir=/nfs

3. Estrutura de Diretórios FTP

Crie no servidor FTP:
/nfs/
├── processed/     # Arquivos processados com sucesso
└── error/         # Arquivos com erro no processamento

🗃️ Estrutura do Banco

Tabela Principal: NF_NFITEM_NFANEXO_XMLFS

Principais campos mapeados:

NR_NOTA_FISCAL - Número da nota fiscal
CD_SERIE_NF - Série da nota
CD_CGC_EMITENTE - CNPJ do emitente
DT_EMISSAO - Data de emissão
VL_TOTAL_NOTA - Valor total da nota
DS_XML_COMPL - XML completo (até 4000 caracteres)
DS_LINK_XML - Nome do arquivo original
🚀 Execução

Desenvolvimento
>mvn spring-boot:run
 
Produção
>mvn clean package
java -jar target/nf-0.0.1-SNAPSHOT.jar

📊 Logs e Monitoramento

Os logs mostram o processo em tempo real:
🚀 MÉTODO AGENDADO EXECUTADO - Verificando XMLs...
📁 Iniciando verificação de XMLs no diretório: /nfs
📊 Total de arquivos encontrados: 3
🔄 Processando arquivo: nota_fiscal.xml
✅ Nota fiscal 12345 processada com sucesso
✅ XML nota_fiscal.xml processado e movido com sucesso

⚠️ Solução de Problemas

Erro: "Não é possível inserir NULL"

Causa: Campos obrigatórios não preenchidos
Solução: Verificar mapeamento no XmlProcessingService

Erro: "Diretório não encontrado"

Causa: Caminho FTP incorreto
Solução: Verificar ftp.xmls-dir no application.properties

Arquivos não são processados

Causa: Extensão diferente de .xml
Solução: Certificar que arquivos terminam com .xml

🔧 Customização

Alterar Intervalo de Processamento

Edite application.properties:
>file.processing.delay=30000  # 30 segundos

Adicionar Novos Campos

Adicione campo na entidade NfXmlEntity
Atualize mapeamento no XmlProcessingService
Execute mvn clean compile
📝 Estrutura do Projeto
src/
├── main/
│   ├── java/
│   │   └── financeiro/
│   │       └── nf/
│   │           ├── NfApplication.java
│   │           ├── entity/
│   │           │   └── NfXmlEntity.java
│   │           ├── repository/
│   │           │   └── NfXmlRepository.java
│   │           └── services/
│   │               ├── FtpService.java
│   │               ├── XmlProcessingService.java
│   │               └── NfeProcessingService.java
│   └── resources/
│       └── application.properties
└── pom.xml

👥 Desenvolvimento

Adicionando Novas Funcionalidades

Crie testes unitários para novas features
Atualize documentação
Execute mvn clean test antes do commit
Padrões de Código

Use Lombok para getters/setters
Logs em português com emojis para melhor visualização
Tratamento de exceptions específico
📞 Suporte

Para issues e dúvidas:

Verifique os logs da aplicação
Confirme configurações do FTP e banco
Verifique permissões de diretório
📄 Licença

Este projeto é para uso interno.