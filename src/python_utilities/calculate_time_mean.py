
import json
from pathlib import Path
from statistics import mean

def calculate_mean_elapsed_time():
    """Calculate the mean of elapsedTime from JSON files starting with 'printer1'."""
    input_dir = Path("src/output/output_jsons")
    elapsed_times = []

    # Iterate through JSON files starting with 'printer1'
    for json_file in input_dir.glob("printer1*.json"):
        with open(json_file, 'r') as file:
            data = json.load(file)
            elapsed_time = data.get("elapsedTime")
            if elapsed_time is not None:
                elapsed_times.append(elapsed_time)

    # Calculate the mean if there are elapsedTime values
    if elapsed_times:
        mean_elapsed_time = mean(elapsed_times)
        print(f"Mean elapsedTime: {mean_elapsed_time:.2f}")
    else:
        print("No elapsedTime values found in 'printer1' files.")

if __name__ == "__main__":
    calculate_mean_elapsed_time()
