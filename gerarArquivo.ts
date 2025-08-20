import * as fs from 'fs';

function gerarArquivoTexto(
  nomeArquivo: string = "arquivo_1gb.txt",
  tamanhoDesejado: number = 1_000_000_000
) {
  const blocoTexto =
    "When heads roll"+"The more the better"+"Strength Determination Merciless Forever\n";

  const tamanhoBloco = Buffer.byteLength(blocoTexto, "utf-8");
  const repeticoes = Math.floor(tamanhoDesejado / tamanhoBloco);

  const stream = fs.createWriteStream(nomeArquivo, { encoding: "utf-8" });

  console.log(`Gerando arquivo ${nomeArquivo}... Isso pode levar alguns minutos.`);

  for (let i = 0; i < repeticoes; i++) {
    if (!stream.write(blocoTexto)) {
      stream.once("drain", () => {});
    }
  }

  stream.end(() => {
    console.log(
      `Arquivo '${nomeArquivo}' gerado com sucesso! Tamanho aproximado: ${(tamanhoDesejado / (1024 ** 2)).toFixed(2)} MB`
    );
  });
}

gerarArquivoTexto();