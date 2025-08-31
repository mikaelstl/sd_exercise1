import os

def gerar_arquivo_texto(nome_arquivo='input_mapreduce.txt', tamanho_desejado=1_073_741_824):
    file_path = os.path.join(os.getcwd(), 'shared', 'data', nome_arquivo)
    
    bloco_texto = (
        "Este é um exemplo de texto repetitivo para preencher um arquivo grande. "
        "Você pode personalizar este conteúdo conforme quiser. "
        "Apenas certifique-se de que o tamanho total atinja cerca de 1GB.\n"
    )
    
    tamanho_bloco = len(bloco_texto.encode('utf-8'))
    repeticoes = tamanho_desejado // tamanho_bloco

    with open(file_path, 'w', encoding='utf-8') as f:
        for _ in range(repeticoes):
            f.write(bloco_texto)

    print(f"Arquivo '{nome_arquivo}' gerado com sucesso! Tamanho aproximado: {tamanho_desejado // (1024**2)} MB")

if __name__ == "__main__":
    gerar_arquivo_texto()