package calendarApp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileManager {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        //Horario horario = new Horario();
        //horario.lerCSV(new File("ES/input.csv"));
        //saveInJSON(horario,"output.json");
        convertJSONtoCSV(new File(new File("").getAbsolutePath() + File.separator + "ES/validtest.json"), "output.csv");
    }

    /**
     *
     * @param inputFile ficheiro CSV para converter para JSON
     * @param outputFilePath caminho onde ficheiro JSON irá ser guardado
     */
    public static void convertCSVtoJSON(File inputFile, String outputFilePath) {

        File jsonFile = new File(outputFilePath);
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = CsvSchema.builder().setColumnSeparator(';').setUseHeader(true).build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (MappingIterator<Object> mappingIterator = csvMapper.readerFor(Map.class).with(csvSchema).readValues(inputFile)) {
            objectMapper.writeValue(jsonFile, mappingIterator.readAll());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param inputFile ficheiro JSON para converter para CSV
     * @param outputFilePath caminho onde ficheiro CSV irá ser guardado
     */
    public static void convertJSONtoCSV(File inputFile, String outputFilePath) {
        File csvFile = new File(outputFilePath);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> data = objectMapper.readValue(inputFile, new TypeReference<>() {
            });
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            String[] headers = data.get(0).keySet().toArray(new String[0]);
            writer.writeNext(headers);
            for (Map<String, String> row : data)
                writer.writeNext(row.values().toArray(new String[0]));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Este método cria um ficheiro JSON a partir de um horário fornecido.
     *
     * @param horario objeto Horario para guardar em ficheiro JSON
     * @param outputFilePath caminho onde ficheiro JSON irá ser guardado
     */
    static public void saveInJSON(Horario horario, String outputFilePath) {
        if (horario == null) {
            throw new NullPointerException("O horario fornecido é igual a null");
        }
        List<Map<String, String>> data = new ArrayList<>();
        for (Aula a : horario.getAulas()) {
            HashMap<String, String> aulaData = new HashMap<>();
            aulaData.put("Curso", listToString(a.cursos()));
            aulaData.put("Unidade Curricular", a.uc());
            aulaData.put("Turno", a.turno().nome());
            aulaData.put("Turma", listToString(a.turmas()));
            aulaData.put("Inscritos no turno", Integer.toString(a.turno().numInscritos()));
            aulaData.put("Dia da semana", a.diaDaSemana());
            aulaData.put("Hora início da aula", a.horaInicio().format(TIME_FORMATTER));
            aulaData.put("Hora fim da aula", a.horaFim().format(TIME_FORMATTER));
            if (a.data() != null)
                aulaData.put("Data da aula", a.data().format(DATE_FORMATTER));
            else
                aulaData.put("Data da aula", "");
            if (!a.sala().nome().isBlank())
                aulaData.put("Sala atribuída à aula", a.sala().nome());
            else
                aulaData.put("Sala atribuída à aula", "");
            if (a.sala().lotacao() != 0)
                aulaData.put("Lotação da sala", Integer.toString(a.sala().lotacao()));
            else
                aulaData.put("Lotação da sala", "");
            data.add(aulaData);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        File jsonFile = new File(outputFilePath);

        try {
            objectMapper.writeValue(jsonFile, data);
        } catch (IOException e) {
            System.err.println("saveInJSON(h, outputFilePath): Erro ao escrever no ficheiro");
        }
    }

    /**
     * Este método cria um ficheiro CSV a partir de um horário fornecido.
     *
     * @param horario objeto Horario para guardar em ficheiro CSV
     * @param caminhoDeOutput caminho onde ficheiro CSV irá ser guardado
     */
    static public void saveInCSV(Horario horario, String caminhoDeOutput) {
        if (horario == null) {
            throw new NullPointerException("O horario fornecido é igual a null");
        }
        try {
            File csvFile = new File(caminhoDeOutput);
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';', CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
                    writer.writeNext(new String[] {"Curso", "Unidade Curricular", "Turno", "Turma", "Inscritos no turno", "Dia da semana", "Hora início da aula", "Hora fim da aula", "Data da aula", "Sala atribuída à aula", "Lotação da sala"});
                for (Aula a : horario.getAulas()) {
                String[] rowData = { listToString(a.cursos()), a.uc(), a.turno().nome(), listToString(a.turmas()),
                        Integer.toString(a.turno().numInscritos()), a.diaDaSemana(),
                        a.horaInicio().format(TIME_FORMATTER), a.horaFim().format(TIME_FORMATTER),
                        a.data() != null ? a.data().format(DATE_FORMATTER) : "",
                        !a.sala().nome().isBlank() ? a.sala().nome() : "",
                        a.sala().lotacao()!=0 ? Integer.toString(a.sala().lotacao()): ""};
                        writer.writeNext(rowData);

            }
            writer.close();
        } catch (IOException e) {
            System.err.println("gravaEmCSV(h,caminhoDeOutput): Erro ao escrever no ficheiro");
        }
    }


    private static String listToString(List<String> list) {
        if (list.size() == 0)
            return "";
        String str = list.toString();
        return str.substring(1, str.length() - 1);
    }

}
