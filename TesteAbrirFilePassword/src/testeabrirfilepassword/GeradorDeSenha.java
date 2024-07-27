package testeabrirfilepassword;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeradorDeSenha {

    private static final List<Integer> ASCII_VALORES = new ArrayList<>();

    static {
        preencherValoresASCII();
    }

    private static void preencherValoresASCII() {
        for (int i = 48; i < 58; i++) { // 0-9
            ASCII_VALORES.add(i);
        }
        for (int i = 65; i < 91; i++) { // A-Z
            ASCII_VALORES.add(i);
        }
        for (int i = 97; i < 123; i++) { // a-z
            ASCII_VALORES.add(i);
        }
        // Add other special characters as needed
        ASCII_VALORES.add(33); // !
        ASCII_VALORES.add(64); // @
        ASCII_VALORES.add(35); // #
        ASCII_VALORES.add(45); // -
        ASCII_VALORES.add(95); // _
        ASCII_VALORES.add(43); // +
        ASCII_VALORES.add(61); // =
        // Add other special characters
    }

    public static String gerarSenhas(String caminhoArquivo, int comprimentoMaximo) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<String>> futures = new ArrayList<>();

        for (int comprimento = 1; comprimento <= comprimentoMaximo; comprimento++) {
            int finalComprimento = comprimento;
            futures.add(executor.submit(() -> gerarCombinacoes(ASCII_VALORES, finalComprimento, "", caminhoArquivo)));
        }

        for (Future<String> future : futures) {
            try {
                String senha = future.get();
                if (senha != null) {
                    executor.shutdownNow();
                    return senha;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return null;
    }

    private static String gerarCombinacoes(List<Integer> caracteres, int comprimento, String prefixo,
            String caminhoArquivo) {
        if (prefixo.length() == comprimento) {
            if (TesteAbrirFilePassword.testaSenha(caminhoArquivo, prefixo)) {
                return prefixo;
            }
            return null;
        }

        for (int i = 0; i < caracteres.size(); i++) {
            String resultado = gerarCombinacoes(caracteres, comprimento, prefixo + (char) caracteres.get(i).intValue(),
                    caminhoArquivo);
            if (resultado != null) {
                return resultado;
            }
        }
        return null;
    }
}
