package calendarApp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static calendarApp.FileManager.saveInCSV;


public class Horario {

    private final List<Aula> aulas;

    public Horario() {
        this.aulas = new ArrayList<>();
    }

    public Horario(List<Aula> aulas) {
        this.aulas = aulas;
    }

    public List<Aula> getAulas() {
        return aulas;
    }

    public void adicionarAula(Aula aula) {
        aulas.add(aula);
    }

    public void removerAula(calendarApp.Aula aula) {
        aulas.remove(aula);
    }

    /**
     * Este método cria e adiciona aulas a {@code this} a partir do ficheiro fornecido.
     *
     * @param ficheiro ficheiro CSV para ler
     * @throws Exception quando existe um erro ao ler o ficheiro CSV
     */
    public void lerCSV(File ficheiro) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
            br.readLine(); // serve para descartar a primeira linha
            String linha;
            while ((linha = br.readLine()) != null)
                criarAulaCSV(linha);
        } catch (Exception e) {
            throw new Exception("Erro a ler ficheiro CSV " + ficheiro.getName() + ", " + e.getMessage());
        }
    }


    private void criarAulaCSV(String linha) {
        String[] campos = linha.split(";", -1);
        List<String> cursos = Arrays.stream(campos[0].split(", ")).toList();
        Turno turno = new Turno(campos[2], Integer.parseInt(campos[4]));
        List<String> turmas = Arrays.stream(campos[3].split(", ")).toList();
        LocalDate data = campos[8].isEmpty() ? null : LocalDate.parse(campos[8], DateTimeFormatter.ofPattern("dd/MM/yyyy")); // TODO Decidir se aula de horário sem data é para descartar ou não
        String nomeSala = campos[9].isEmpty() ? "" : campos[9];
        int lotacaoSala = campos[10].isEmpty() ? 0 : Integer.parseInt(campos[10]);
        Sala sala = new Sala(nomeSala, lotacaoSala);
        Aula aula = new Aula(cursos, campos[1], turno, turmas, campos[5], LocalTime.parse(campos[6]), LocalTime.parse(campos[7]), sala, data);
        adicionarAula(aula);
    }

    /**
     * Este método cria e adiciona aulas a {@code this} a partir do ficheiro fornecido.
     *
     * @param ficheiro ficheiro JSON para ler
     * @throws Exception quando existe um erro ao ler o ficheiro JSON
     */
    public void lerJSON(File ficheiro) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Map<String, String>> data = objectMapper.readValue(ficheiro, new TypeReference<>() {
            });
            for (Map<String, String> row : data)
                criarAulaJSON(row);
        } catch (Exception e) {
            throw new Exception("Erro a ler ficheiro JSON " + ficheiro.getName() + ", " + e.getMessage());
        }
    }


    private void criarAulaJSON(Map<String, String> row) {
        List<String> cursos = Arrays.asList(row.get("Curso").split(", "));
        Turno turno = new Turno(row.get("Turno"), Integer.parseInt(row.get("Inscritos no turno")));
        List<String> turmas = Arrays.asList(row.get("Turma").split(", "));
        LocalDate dataAula = row.get("Data da aula").isEmpty() ? null : LocalDate.parse(row.get("Data da aula"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String nomeSala = row.get("Sala atribuída à aula").isEmpty() ? "" : row.get("Sala atribuída à aula");
        int lotacaoSala = row.get("Lotação da sala").isEmpty() ? 0 : Integer.parseInt(row.get("Lotação da sala"));
        Sala sala = new Sala(nomeSala, lotacaoSala);
        Aula aula = new Aula(cursos, row.get("Unidade Curricular"), turno, turmas, row.get("Dia da semana"), LocalTime.parse(row.get("Hora início da aula")), LocalTime.parse(row.get("Hora fim da aula")), sala, dataAula);
        adicionarAula(aula);
    }

    /**
     *
     * @return lista com todas as ucs associadas ás aulas
     */
    public List<String> getUcs(){
        List<String> list = new ArrayList<>();
        for (Aula a : aulas) {
            if (!list.contains(a.uc()))
                list.add(a.uc());
        }
        return list;
    }


    /**
     *
     * @param ucsEscolhidas recebe lista de Strings representando as ucs escolhidas
     * @return retorna um novo objeto Horario com apenas as aulas das ucs escolhidas
     */
    public Horario criarHorario(List<String> ucsEscolhidas,String path){
        for (Aula aula : this.getAulas()){
            if(!ucsEscolhidas.contains(aula.uc())){
                this.removerAula(aula);
            }
        }
        saveInCSV(this,path);
        return this;
    }

    /**
     *
     * @return true se houver sobreposição de aulas na mesma sala, false caso contrário
     */

    public boolean checkForColisions() {
        aulas.sort(Comparator.naturalOrder());
        for (int i = 0; i < aulas.size() - 1; i++) {
            Aula a = aulas.get(i);
            Aula b = aulas.get(i + 1);
            if (a.sala().equals(b.sala()) && doTheyOverlap(a, b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param a,b recebe duas Aulas que irão ser comparadas
     * @return true se houver sobreposições
     */
    public boolean doTheyOverlap(Aula a, Aula b) {
        if(!a.data().equals(b.data()))
            return false;
        return !a.horaInicio().isAfter(b.horaFim()) && !b.horaInicio().isAfter(a.horaFim());
    }

    /**
     *
     * @return true se houver alguma aula numa sala onde o número de inscritos é maior que a lotação da sala
     */

    public boolean checkForOverbooking() {
        for (Aula a : aulas)
            if (a.sala().lotacao() < a.turno().numInscritos())
                return true;
        return false;
    }


}
