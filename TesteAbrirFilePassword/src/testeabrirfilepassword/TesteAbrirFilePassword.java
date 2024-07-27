package testeabrirfilepassword;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class TesteAbrirFilePassword {

    public static final String CAMINHO = "C:\\Users\\Kaiky Pires\\Downloads\\Trabalho-Saulo\\projeto e arquivos para o problema da senha (1)\\senha\\arquivosTP\\";
    private static final List<String> senhasEncontradas = new ArrayList<>(); // Lista para armazenar senhas encontradas

    public static void main(String[] args) {
        String[] arquivosMenores = {
                CAMINHO + "doc1.zip",
                CAMINHO + "doc2.zip",
                CAMINHO + "doc3.zip",
                CAMINHO + "doc4.zip"
        };

        ExecutorService executorService = Executors.newFixedThreadPool(arquivosMenores.length);
        List<Future<String>> futureList = new ArrayList<>();

        // Submete tarefas para o executor
        for (String arquivo : arquivosMenores) {
            Future<String> future = executorService.submit(new SenhaTask(arquivo));
            futureList.add(future);
        }

        // Espera pelas tarefas terminarem e coleta os resultados
        for (Future<String> future : futureList) {
            try {
                String senha = future.get();
                if (senha != null) {
                    System.out.println("Senha encontrada: " + senha);
                    synchronized (senhasEncontradas) {
                        senhasEncontradas.add(senha); // Adiciona a senha encontrada à lista
                    }
                } else {
                    System.out.println("Nenhuma senha encontrada.");
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Erro ao obter resultado: " + e.getMessage());
            }
        }

        executorService.shutdown();

        testarSenhaFinal();
    }

    private static void testarSenhaFinal() {
        String senhaFinal = combinarSenhasQuebradas();
        String caminhoArquivoFinal = CAMINHO + "final.zip";

        if (testaSenha(caminhoArquivoFinal, senhaFinal)) {
            System.out.println("Senha do arquivo principal encontrada: " + senhaFinal);
        } else {
            System.out.println("Não foi possível encontrar a senha do arquivo principal.");
        }
    }

    private static String combinarSenhasQuebradas() {
        
        StringBuilder senhaFinal = new StringBuilder();
        synchronized (senhasEncontradas) {
            for (String senha : senhasEncontradas) {
                senhaFinal.append(senha); 
            }
        }
        return senhaFinal.toString(); 
    }

    public static boolean testaSenha(String caminhoArquivo, String senha) {
        try (ZipFile zipFile = new ZipFile(new File(caminhoArquivo))) {
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(senha.toCharArray());
            }
            List<FileHeader> fileHeaderList = zipFile.getFileHeaders();
            if (fileHeaderList.isEmpty()) {
                System.out.println("Nenhum arquivo encontrado no zip: " + caminhoArquivo);
                return false;
            }
            for (FileHeader fileHeader : fileHeaderList) {
                try {
                    zipFile.extractFile(fileHeader, CAMINHO);
                    System.out.println("Senha encontrada para " + caminhoArquivo + ": " + senha);
                    return true;
                } catch (ZipException e) {
                    // Log específico para erro de extração
                    System.err.println("Erro ao extrair o arquivo: " + fileHeader.getFileName() + " com senha: " + senha);
                }
            }
        } catch (ZipException e) {
            // Log detalhado para depuração
            System.err.println("Erro ao tentar a senha: " + senha + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        }
        return false;
    }

    // Classe interna para lidar com as tarefas de senha
    static class SenhaTask implements Callable<String> {
        private final String caminhoArquivo;

        public SenhaTask(String caminhoArquivo) {
            this.caminhoArquivo = caminhoArquivo;
        }

        @Override
        public String call() {
            return GeradorDeSenha.gerarSenhas(caminhoArquivo, 3);
        }
    }
}
