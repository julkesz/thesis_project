import os
import sys
import json
from pathlib import Path
from collections import defaultdict
from statistics import stdev

def calculate_max_execution_time(data):
    """Calculate the maximum stop time for the given printer's schedule."""
    if len(data["schedule"]) == 0:
        return 0
    else:
        return max(timeslot["stop"] for timeslot in data["schedule"])

def calculate_filament_changes(data):
    """Calculate the number of filament changes for the given printer's schedule."""
    previous_filament = None
    filament_changes = 0

    for timeslot in data["schedule"]:
        current_filament = timeslot["tasks"][0]["filament"] if timeslot["tasks"] else None

        if previous_filament is not None and current_filament != previous_filament:
            filament_changes += 1

        previous_filament = current_filament

    return filament_changes

def calculate_board_occupancies(data):
    """Calculate board occupancy for each timeslot in the printer's schedule."""
    board_area = data["boardWidth"] * data["boardLength"]
    occupancies = []

    for timeslot in data["schedule"]:
        occupied_area = sum(task["width"] * task["length"] for task in timeslot["tasks"])
        occupancy_percentage = round(occupied_area / board_area, 4) if board_area > 0 else 0
        occupancies.append(occupancy_percentage)

    return occupancies

def calculate_order_tardiness(timestamp):
    """Calculate end time and tardiness for each order across all printers."""
    input_dir = Path("src/output/output_jsons")
    orders = defaultdict(lambda: {"endTime": 0, "deadline": None})

    for json_file in input_dir.glob(f"*schedule_{timestamp}.json"):
        with open(json_file, 'r') as file:
            data = json.load(file)

            for timeslot in data["schedule"]:
                for task in timeslot["tasks"]:
                    order_number = task["orderNumber"]
                    deadline = task["deadline"]
                    stop_time = timeslot["stop"]

                    if stop_time > orders[order_number]["endTime"]:
                        orders[order_number]["endTime"] = stop_time
                    orders[order_number]["deadline"] = deadline

    tardiness_list = []
    for order_number, order_data in orders.items():
        end_time = order_data["endTime"]
        deadline = order_data["deadline"]
        tardiness = end_time - deadline
        tardiness_list.append({
            "orderNumber": order_number,
            "endTime": end_time,
            "tardiness": tardiness
        })

    return tardiness_list

def calculate_height_standard_deviations(data):
    """Calculate standard deviation of task heights for each timeslot."""
    height_std_devs = []

    for timeslot in data["schedule"]:
        heights = [task["height"] for task in timeslot["tasks"]]

        if len(heights) > 1:
            std_dev = round(stdev(heights), 4)  # Calculate and round to 4 decimal places
        else:
            std_dev = 0  # If there's 1 or no task, standard deviation is 0

        height_std_devs.append(std_dev)

    return height_std_devs

def calculate_statistics(timestamp):
    input_dir = Path("src/output/output_jsons")
    output_dir = Path("src/output/output_statistics")
    output_dir.mkdir(parents=True, exist_ok=True)

    printer_statistics = {
        "executionTimes": {},
        "filamentChanges": {},
        "boardOccupancies": {},
        "tardiness": [],
        "taskHeightStandardDeviations": {}
    }

    for json_file in input_dir.glob(f"*schedule_{timestamp}.json"):
        with open(json_file, 'r') as file:
            data = json.load(file)
            printer_name = data["printerName"]

            max_execution_time = calculate_max_execution_time(data)
            filament_changes = calculate_filament_changes(data)
            board_occupancies = calculate_board_occupancies(data)
            height_std_devs = calculate_height_standard_deviations(data)

            printer_statistics["executionTimes"][printer_name] = max_execution_time
            printer_statistics["filamentChanges"][printer_name] = filament_changes
            printer_statistics["boardOccupancies"][printer_name] = board_occupancies
            printer_statistics["taskHeightStandardDeviations"][printer_name] = height_std_devs

    printer_statistics["tardiness"] = calculate_order_tardiness(timestamp)

    output_file = output_dir / f"printing_statistics_{timestamp}.json"
    with open(output_file, 'w') as file:
        json.dump(printer_statistics, file, indent=4)

    print(f"Statistics calculated and saved to {output_file}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python calculate_statistics.py <timestamp>")
        sys.exit(1)

    timestamp = sys.argv[1]
    calculate_statistics(timestamp)
