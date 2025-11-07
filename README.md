# Flight-CLI-Prettifier

A lightweight Java command-line utility that converts raw flight itinerary text into human-friendly output. It expands IATA and ICAO airport codes into full airport and city names, normalizes ISO dates and times (including timezone offset notes), removes control characters, and highlights key fields using color and emojis for quick readability.

Designed for developers and operations teams who handle plain-text flight logs or itineraries, the tool supports interactive color selection and produces both console-friendly and file-based formatted output.

---

## Key features

- **Expand IATA and ICAO codes to full airport names and optional city-only lookups**
  - IATA codes (e.g., `#LHR` → London Heathrow Airport)
  - ICAO codes (e.g., `##KJFK` → John F. Kennedy International Airport)
  - City names (e.g., `*#LHR` or `*##KJFK` → London or New York)

- **Format ISO dates and times into human-readable forms and append timezone notes for non‑standard offsets**
  - Dates (e.g., `D(2007-04-05T12:30−02:00)` → `05 Apr 2007`)
  - Times with timezone (e.g., `T12(2007-04-05T12:30−02:00)` → `12:30PM (-02:00)` with a timezone note)
  - Adds notes for non-standard time zones (e.g., `"-02:00 means 2 hours behind standard time"`)

- **Interactive highlight color selection** (red, green, yellow, blue, purple) for important fields

- **Robust input validation**: Argument checks, CSV schema verification, control-character cleanup, and exception-safe parsing

---

## Code Overview

The main logic is structured as follows:

1. **Argument Validation**:
   Verifies command-line arguments are correctly provided.

2. **File Validation**:
   Checks if input files exist and if the CSV is properly formatted.

3. **Color Selection**:
   Prompts the user to choose a color for highlights.

4. **Text Processing**:
   Reads and processes the input file, replacing codes and formatting time.

5. **Output Generation**:
   Displays formatted content on the console, then writes it to `output.txt`.

---

## Quick start

- **Java Development Kit (JDK)**: Version 8 or above is required

- **Clone the repository**:  
  ```bash
  git clone https://github.com/zoe-here/Flight-CLI-Prettifier.git
  ```
- **Navigate to the project directory**:  
  ```bash
  cd Flight-CLI-Prettifier
  ```

- **Run with the precompiled JAR  (no Maven needed)**:
  ```bash
  java -jar Prettifier.jar input.txt output.txt airport-lookup.csv
  ```
- **Or compile from source**:
  ```bash
  javac prettifier.java
  ```
- **Then run the compiled class**:
  ```bash
  $ java Prettifier.java ./input.txt ./output.txt ./airport-lookup.csv
  ```
- **Optional (Maven dependency)**: If you build with Maven, add OpenCSV
  ```xml
  <dependency>
      <groupId>com.opencsv</groupId>
      <artifactId>opencsv</artifactId>
      <version>5.9</version>
  </dependency>
  ```
- **CSV specification**:  
**The airport lookup CSV must contain 6 columns with the following headers**:  
`name`, `iso_country`, `municipality`, `icao_code`, `iata_code` and `coordinates`

---

## Usage

### Arguments
- `input.txt`: Source file containing the itinerary text  

- `output.txt`: Destination file for the formatted output  

- `airport-lookup.csv`: CSV file containing airport data (IATA/ICAO codes, names, cities)  


Choose your preferred highlight color when prompted


### Example Input

```bash
Flight to #LHR from ##KJFK
Departure: T24(14:30+01:00)
```

### Example Output

```bash
Flight to London Heathrow Airport from John F. Kennedy International Airport
Departure: 14:30 (+01:00) 🕒 Note: +01:00 means 1 hour ahead standard time
```

---

## Error Handling

- Displays usage instructions when arguments are missing or invalid

- Verifies the input and airport lookup files exist before processing

- Checks the airport lookup file has the correct format and no empty cells

- Stops and reports malformed CSV or parsing errors

- Exceptions guarded with try-catch and clear error messages


