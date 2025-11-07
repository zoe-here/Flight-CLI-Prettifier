package itinerary;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class Prettifier {
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CLOCK_EMOJI = "\uD83D\uDD52";

    private static final Pattern iataPattern = Pattern.compile("#([A-Z]{3})");
    private static final Pattern icaoPattern = Pattern.compile("##([A-Z]{4})");
    private static final Pattern cityPattern = Pattern.compile("\\*(#([A-Z]{3})|##([A-Z]{4}))");

    public static String userColorChoice;

    public static void main(String[] args) {
        // Command line argument handling
        if (!validateArgs(args)) return;
        
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        File airportLookupFile = new File(args[2]);

        if (!fileExist(inputFile, "Input") || !fileExist(airportLookupFile, "Airport lookup")) return;

        // Validate and process the airport lookup file
        Map<String, Map<String, String>> maps = null; 
        if (isMalformed(airportLookupFile)) {
            return;
        } else {
            maps = getMaps(airportLookupFile);
        }
        // Ask user for preferred color, store it as static field
        userColorChoice = askForColor();  
        
        // Check input file, process content
        List<String> checkedContent = checkInput(inputFile, maps.get("airportMap"), maps.get("cityMap"));
        writeToFile(checkedContent, outputFile);
    }
    
    private static boolean validateArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("No command line arguments provided.\nAdd -h to see the usage.");
            return false;
        } else if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Usage:\n$ java Prettifier.java ./input.txt ./output.txt ./airport-lookup.csv");
            return false;
        } else if (args.length != 3) {
            System.out.println("Incorrect number of arguments.\nEnter java Prettifier.java -h to see the usage.");
            return false;
        }
        return true;
    }

    private static boolean fileExist(File file, String str) {
        if (!file.exists()) {
            System.out.println(str + " file not found: " + file.getPath());
            return false;
        }
        return true;
    }

    private static boolean isMalformed(File airportLookupFile) { 
        try (CSVReader csvReader = new CSVReader(new FileReader(airportLookupFile))){ 
            List<String[]> allData = csvReader.readAll();
            // Collect all the issues found and display them all at once
            List<String> errors = new ArrayList<>();
        
            // Track row numbers for clearer error reporting
            for (int rowIndex = 0; rowIndex < allData.size(); rowIndex++) { 
                String[] row = allData.get(rowIndex);
                // Check column count
                if (row.length != 6)  {
                    errors.add("Expected 6 columns in Row " + (rowIndex + 1) + ", but found " + row.length + ".");
                } else {
                    // Check for empty cells only if row has 6 columns
                    boolean emptyCellFound = false;
                    for (int coIndex = 0; coIndex < 6; coIndex++) { 
                        if (row[coIndex].trim().isEmpty()) {
                            emptyCellFound = true;
                            break;
                        } 
                    }
                    if (emptyCellFound) {
                        errors.add("Empty cell found in Row " + (rowIndex + 1) + ".");
                    }
                }
            }
            // Display all errors, if any
            if (!errors.isEmpty()) {
                System.out.println("Airport lookup malformed:");
                errors.forEach(System.out::println);
                return true;
            }
            return false;
        } catch (IOException | CsvException e) {
            System.out.println("Error processing airport lookup file: " + e.getMessage());
            return true;
        }
    }

    private static Map<String, Map<String, String>> getMaps(File airportLookupFile) {
        Map<String, String> airportMap = new HashMap<>();
        Map<String, String> cityMap = new HashMap<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(airportLookupFile))) {
            List<String[]> allData = csvReader.readAll();
            
            // Extract headers and build header map
            String[] headers = allData.get(0);
            HashMap<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i], i);
            }
            // Iterate through remaining rows
            for (int rowIndex = 1; rowIndex < allData.size(); rowIndex++) {
                String[] row = allData.get(rowIndex);
                // Retrieve the values
                String icaoCode = row[headerMap.get("icao_code")];
                String iataCode = row[headerMap.get("iata_code")];
                String airportName = row[headerMap.get("name")];
                String cityName = row[headerMap.get("municipality")];
    
                // Add mappings for airport names
                airportMap.put("#" + iataCode, airportName);
                airportMap.put("##" + icaoCode, airportName);

                // Add mappings for city names
                cityMap.put("*#" + iataCode, cityName);
                cityMap.put("*##" + icaoCode, cityName);
            }
        } catch (IOException | CsvException e) {
            System.out.println("Error reading airport lookup file: " + e.getMessage());
        }
        // Create a map to hold both maps
        Map<String, Map<String, String>> maps = new HashMap<>();
        maps.put("airportMap", airportMap);
        maps.put("cityMap", cityMap);
        return maps;
    }

    private static final Map<Integer, String> colorCodes = Map.of(
        1, "\u001B[31m", // Red
        2, "\u001B[32m", // Green
        3, "\u001B[33m", // Yellow
        4, "\u001B[34m", // Blue
        5, "\u001B[35m"  // Purple
    );
    
    private static String askForColor() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) { // Loop until a valid choice is made
                System.out.println("\nPlease choose your preferred color by number: ");
                System.out.println("\n1. Red\n2. Green\n3. Yellow\n4. Blue\n5. Purple\n");
                
                int choice;
                try {
                    choice = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number from 1 to 5.");
                    scanner.next(); // Clear the invalid input
                    continue;
                }
                if (choice < 1 || choice > colorCodes.size()) {
                    System.out.println("Invalid choice. Please choose a number from 1 to 5.");
                    continue;
                }
                String color = colorCodes.get(choice);
                System.out.println(color + "You chose this color." + RESET);
                return color;
            }
        }
    }
    
    private static String addNote(String offset) {
        //Convert the hour part substring to an integer ("-02:00" to "2")
        int hours = Integer.parseInt(offset.substring(1, 3));
        String direction = offset.startsWith("-") ? "behind" : "ahead";
        String parsedOffset = String.format("%d hour%s %s", 
            hours, 
            hours == 1 ? "" : "s", 
            direction);
                
        String note = String.format(" %s Note: %s means %s standard time",
            CLOCK_EMOJI,
            offset,
            BOLD + parsedOffset + RESET);
        return userColorChoice + note + RESET;
    }

    private static List<String> checkInput(File inputFile, Map<String, String> airportMap, Map<String, String> cityMap) {
        List<String> checkedContent = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Replace control characters
                line = line.replace("\\v", "\n")   
                           .replace("\\f", "\n")   
                           .replace("\\r", "\n");
                // Check and replace airport codes
                line = checkAirportCodes(line, airportMap, cityMap);
                // Check and replace time
                line = checkTime(line);
                checkedContent.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading input file: " + inputFile.getPath() + " - " + e.getMessage());
        }
        return checkedContent;
    }

    private static String checkAirportCodes(String str, Map<String, String> airportMap, Map<String, String> cityMap) {
        str = replaceCode(str, cityPattern.matcher(str), cityMap);
        str = replaceCode(str, iataPattern.matcher(str), airportMap);
        str = replaceCode(str, icaoPattern.matcher(str), airportMap);
        return str;
    }
    
    private static String replaceCode(String str, Matcher matcher, Map<String, String> map) {
        while (matcher.find()) {
            String code = matcher.group();
            String name = map.get(code);

            // Apply user's color choice if name is found
            if (name != null) {
                String coloredName = userColorChoice + name + RESET;
                str = str.replace(code, coloredName);
            } 
        }
        return str;
    }

    private static String checkTime(String str) {
        Pattern pattern = Pattern.compile("(D|T12|T24)\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(str);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String formatType = matcher.group(1); // Capture format type (D, T12, or T24)
            String dateTime = matcher.group(2); // Capture date/time string
            
            String formatted = null;

            try {
                if (formatType.equals("D")) {
                    // Extract and format the date part
                    String datePart = dateTime.split("T")[0];
                    formatted = formatDate(datePart);
                } else if (formatType.equals("T12") || formatType.equals("T24")) {
                    // Extract and format the time part
                    String timePart = dateTime.split("T")[1];
                    formatted = formatTime(timePart, formatType);
                }
            } catch (DateTimeParseException e) {
                System.out.println("Error parsing date/time: " + dateTime);
                formatted = matcher.group(); // Keep original if formatting fails
            }

            if (formatted != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(formatted));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String formatDate(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");  
        LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
        return parsedDate.format(outputFormatter);
    }

    private static String formatTime(String date, String formatType) {
        String time;
        String offset = "+00:00";
        // Check if the date ends with "Z" for Zulu time
        if (date.endsWith("Z")) {
            time = date.substring(0, date.length() - 1); // Remove "Z" from the end
        } else {
            // Split by "+" or "-" to separate time and offset
            String[] timeParts = date.split("[+-]");
            time = timeParts[0];
            // Capture original offset, including "+" or "-"
            offset = date.substring(time.length());
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime parsedTime = LocalTime.parse(time, formatter);

        // Adjust formatter based on 12-hour or 24-hour format type
        if (formatType.equals("T12")) {
            formatter = DateTimeFormatter.ofPattern("hh:mma");
        }
        // Format and return time with the offset
        String formattedTime = parsedTime.format(formatter) + " (" + offset + ")";

        // Use addNote to add a user-friendly message about the offset
        if (!offset.equals("+00:00")) { // Only for non-standard offsets
            formattedTime += " " + addNote(offset);
        }
        return formattedTime;
    }

    private static List<String> cleanUp(List<String> lines) {
        List<String> cleanedLines = new ArrayList<>();
        boolean previousLineWasEmpty = false;
        
        for (String line : lines) {
            line = line.trim();

            if (!line.isEmpty()) {
                cleanedLines.add(line);
                previousLineWasEmpty = false;
            } else if (!previousLineWasEmpty) {
                cleanedLines.add("");
                previousLineWasEmpty = true;
            }
        }
        return cleanedLines;
    }

    private static void writeToFile(List<String> checkedContent, File outputFile) {
        // Clean up before writing
        List<String> cleanedContent = cleanUp(checkedContent);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String line : cleanedContent) {
                System.out.println(line);
                String plainTextLine = line.replaceAll("\u001B\\[[;\\d]*m", "");
                writer.write(plainTextLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}