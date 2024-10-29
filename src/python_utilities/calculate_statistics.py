import os
import sys
import json
from pathlib import Path

def calculate_max_execution_time(timestamp):
    # Directory paths
    input_dir = Path("src/output/output_jsons")
    output_dir = Path("src/output/output_statistics")

    # Dictionary to store results for each printer
    printer_statistics = {}

    # Iterate through each file in the input directory
    for json_file in input_dir.glob(f"*schedule_{timestamp}.json"):
        print("HELOS")
        with open(json_file, 'r') as file:
            data = json.load(file)
            printer_name = data["printerName"]
            # Find the maximum 'stop' time in the schedule
            max_stop_time = max(schedule["stop"] for schedule in data["schedule"])
            printer_statistics[printer_name] = max_stop_time

    # Output file path
    output_file = output_dir / f"printing_statistics_{timestamp}.json"
    with open(output_file, 'w') as file:
        json.dump(printer_statistics, file, indent=4)

    print(f"Max execution times calculated and saved to {output_file}")

if __name__ == "__main__":
    # Check if the timestamp argument is provided
    if len(sys.argv) != 2:
        print("Usage: python calculate_statistics.py <timestamp>")
        sys.exit(1)

    # Get the timestamp from command-line arguments
    timestamp = sys.argv[1]
    calculate_max_execution_time(timestamp)
