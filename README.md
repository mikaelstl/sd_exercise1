# Projeto MapReduce

Esse repositório contém código de um projeto MapReduce da disciplina "Sistemas Distribuídos" do IFPB - Campus Cajazeiras. Os requisitos necessários para a execução desse projeto são:

- JDK versão 17. Download [aqui](https://www.oracle.com/br/java/technologies/downloads/#jdk17) ou use um gerenciador de versões como [SDKMAN!](https://sdkman.io/install) (Recomendado!). Ou, no linux, digite no terminal:
```bash
sudo apt install openjdk17-jdk maven -y
```
- Python [aqui](https://www.python.org/downloads/). Ou, no linux, digite no terminal:
```bash
sudo apt-get install python3.9 -y
```
- Docker [aqui](https://docs.docker.com/engine/), selecione "Install Docker Engine". Ou, no linux, digite no terminal:
```bash
sudo apt install docker.io
```
- Docker-compose [aqui](https://docs.docker.com/compose/install/). Ou, no linux, digite no terminal:
```bash
sudo apt install docker-compose
```

# Executando

Este projeto roda principalmente dentro de containers Docker, então é de muito importancia que tenha o Docker e o docker-compose instalado na sua máquina. Dito isso, ao fazer o clone do repositório, entra na pasta do projeto e digite no terminal:

```bash
sudo python3 gerar_arquivo.py
```
Isso irá gerar o arquivo de input do sistema. Será gerado em /shared/data/.
Com o arquivo gerar digite:

```bash
sudo docker-compose build --no-cache
```
Para garantir que os containeres sejam construídos sem "restos" de dados de outros containeres.

Agora basta digitar:

```bash
sudo docker-compose up
```
E esperar todo o processo acontecer.
Cada etapa será indicada via logs. Após todos os containeres finalizarem, todos os arquivos estaram em /shared/data.

# Definição de pastas em /shared

- `/data`: Será a pasta principal de compartilhamento, guardando cada arquivo resultante dos processos.
- `/data/chunks`: Guardará os chunks, resultados da divisão do arquivo principal.
- `/data/intermediate`: Guardará os arquivos de intermedio entre o mapper e o coordinator, guardará cada palavra e um array com a quantidade de vezes q. apareceu.
- `/data/rinputs`: Contém os arquivos que seram inseridos no reducer, arquivo já com cada palavra filtrada através do seu hash para garantir que ela será processada por um reducer específico.
- `/data/routputs`: Contém os arquivos resultantes dos reducer, cada palavra com seu valor final de quantas vezes apareceu no texto.
- `/data/result`: Guardará o resultado final de toda a operação, será a junção dos arquivos em `/data/routputs`.