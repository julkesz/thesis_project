
import sys
import os
import json
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from matplotlib.patches import Patch, Rectangle


def plot_schedule_from_files(date_time, source_directory, output_directory, scale=1.2, machine_board_size=4000):
    fig, ax = plt.subplots(figsize=(32, 20))  # Adjust the figure size as needed
    colors = list(mcolors.TABLEAU_COLORS.values())
    filament_colors = list(mcolors.CSS4_COLORS.values())

    order_color_map = {}
    filament_color_map = {}
    color_index = 0
    filament_color_index = 0

    machine_schedules = []
    machine = 1

    for filename in sorted(os.listdir(source_directory)):
        if filename.endswith(date_time + '.json'):
            machine_schedule, color_index, filament_color_index = process_machine_schedule_with_occupancy(
                ax, filename, source_directory, colors, filament_colors, order_color_map, filament_color_map,
                color_index, filament_color_index, machine, scale, machine_board_size
            )
            machine_schedules.append((machine, machine_schedule))
            machine += 1

    set_plot_properties(ax, machine, machine_schedules, source_directory, date_time, scale, bar_height=1.0)  # Default height is now 1 (machine fully occupied)

    add_legends(fig, order_color_map, filament_color_map)

    # Save the plot to a file
    plt.savefig(output_directory + '/combined_schedule_plot_' + date_time + '.png', bbox_inches='tight')
    plt.close()

def process_machine_schedule_with_occupancy(ax, filename, source_directory, colors, filament_colors, order_color_map, filament_color_map,
                                            color_index, filament_color_index, machine, scale, machine_board_size):
    filepath = os.path.join(source_directory, filename)

    with open(filepath, 'r') as f:
        data = json.load(f)

    bar_height = 0.8  # Maximum height is 1, which represents 100% occupancy of the machine
    filament_bar_height = 0.1  # Slightly smaller filament bar height since it doesn't depend on task occupancy
    machine_schedule = []

    for item in data['schedule']:
        start = item['start']
        stop = item['stop']
        total_task_area = sum(task['width'] * task['length'] for task in item['tasks'])  # Total area of all tasks in this slot
        task_heights = [task['width'] * task['length'] / machine_board_size for task in item['tasks']]  # Height based on task size
        total_task_height = sum(task_heights)  # Total height occupied by tasks in this timeslot

        # Filament handling
        filament_number = item['tasks'][0]['filament']  # Assuming first task has filament info
        filament_color, filament_color_index = get_color_for_filament(filament_number, filament_colors, filament_color_map, filament_color_index)

        # Plot filament bar
        filament_y_pos = machine - bar_height / 2 - filament_bar_height
        ax.broken_barh([(start * scale, (stop - start) * scale)], (filament_y_pos, filament_bar_height), facecolors=filament_color, edgecolors='black', linewidth=0.5)
        ax.text(((start + stop) / 2) * scale, filament_y_pos + filament_bar_height / 2, f'{filament_number}', ha='center', va='center', color='black', fontsize='x-small')

        # Calculate task positions and plot them
        current_y_pos = machine - bar_height / 2  # Start at the bottom of the slot
        for task_index, task in enumerate(item['tasks']):
            task_height = task_heights[task_index]
            order_number = task['orderNumber']
            task_number = task['taskId']

            # Assign order color
            color, color_index = get_color_for_order(order_number, colors, order_color_map, color_index)

            # Plot task bar based on task height
            ax.broken_barh([(start * scale, (stop - start) * scale)], (current_y_pos, task_height), facecolors=color, edgecolors='black', linewidth=0.5)
            ax.text(((start + stop) / 2) * scale, current_y_pos + task_height / 2, f'{order_number}:{task_number}', ha='center', va='center', color='white', fontsize='small')

            current_y_pos += task_height  # Move up for the next task

        # Fill remaining empty space if the total height of tasks is less than the maximum bar height
        if total_task_height < bar_height:
            empty_space_height = bar_height - total_task_height
            ax.broken_barh([(start * scale, (stop - start) * scale)], (current_y_pos, empty_space_height), facecolors='white', edgecolors='black', linewidth=0.5)

        machine_schedule.append((start, stop))

    return machine_schedule, color_index, filament_color_index

def set_plot_properties(ax, machine, machine_schedules, source_directory, date_time, scale, bar_height):
    # Set limits for axes
    ax.set_ylim(0.5, machine - 0.5)
    ax.set_xlim(0, get_max_stop_time(source_directory, date_time) * scale + 20)

    # Add labels, ticks, and grid
    ax.set_xlabel('Czas [min]')
    ax.set_yticks(range(1, machine))
    ax.set_yticklabels([f'Drukarka {i}' for i in range(1, machine)])
    ax.grid(True, axis='x')

    # Fill gaps in the schedule
    for machine, schedule in machine_schedules:
        fill_schedule_gaps(ax, schedule, machine, bar_height, scale)

    # Add black border around each machine's schedule and filament bar
    add_machine_borders(ax, machine_schedules, bar_height, scale)

# The remaining functions (get_color_for_filament, get_color_for_order, fill_schedule_gaps, add_machine_borders, etc.) stay the same as before.


def add_legends(fig, order_color_map, filament_color_map):
    # Create order number legend
    order_handles = [Patch(color=color, label=f'Zamówienie {order}') for order, color in order_color_map.items()]
    filament_patch = Patch(color='lightgrey', label='Zmiana filamentu')

    # Create filament color legend
    filament_handles = [Patch(color=color, label=f'Filament {filament}') for filament, color in sorted(filament_color_map.items())]

    # Create and position the legends
    fig.legend(handles=order_handles + [filament_patch], loc='upper left', bbox_to_anchor=(0.1, -0.1), bbox_transform=fig.transFigure)
    fig.legend(handles=filament_handles, loc='upper left', bbox_to_anchor=(0.6, -0.1), bbox_transform=fig.transFigure)

def get_color_for_filament(filament_number, filament_colors, filament_color_map, filament_color_index):
    if filament_number not in filament_color_map:
        filament_color_map[filament_number] = filament_colors[filament_color_index % len(filament_colors)]
        filament_color_index += 1
    return filament_color_map[filament_number], filament_color_index

def get_color_for_order(order_number, colors, order_color_map, color_index):
    if order_number not in order_color_map:
        order_color_map[order_number] = colors[color_index % len(colors)]
        color_index += 1
    return order_color_map[order_number], color_index


def get_max_stop_time(source_directory, date_time):
    max_stop = 0
    for filename in os.listdir(source_directory):
        if filename.endswith(date_time + '.json'):
            filepath = os.path.join(source_directory, filename)
            with open(filepath, 'r') as f:
                data = json.load(f)
            stop_times = [item['stop'] for item in data['schedule']]
            max_stop = max(max_stop, max(stop_times))
    return max_stop

def fill_schedule_gaps(ax, schedule, machine, bar_height, scale):
    prev_stop = 0
    for start, stop in schedule:
        if start > prev_stop:
            ax.broken_barh([(prev_stop * scale, (start - prev_stop) * scale)], (machine - bar_height / 2, bar_height), facecolors='lightgrey', edgecolor='black', linewidth=0.5)
        prev_stop = stop

def add_machine_borders(ax, machine_schedules, bar_height, scale):
    filament_bar_height = 0.2
    for machine, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        end = max(end_times)

        # Add border for task schedule
        rect = Rectangle((start * scale, machine - bar_height / 2), (end - start) * scale, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

        # Add border for filament schedule
        filament_rect = Rectangle((start * scale, machine - bar_height / 2 - filament_bar_height), (end - start) * scale, filament_bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(filament_rect)





if __name__ == '__main__':
    # Ensure the timestamp is provided as a command-line argument
    if len(sys.argv) < 2:
        print("Usage: python generate_graph.py <timestamp>")
        sys.exit(1)

    timestamp = sys.argv[1]  # Get timestamp from command-line argument
    json_directory = 'src/output/output_jsons'
    plot_directory = 'src/output/output_plots'

    # Plot the schedules from JSON files and save the plot to a file
    plot_schedule_from_files(timestamp, json_directory, plot_directory)

    print(f"The plot has been saved to {plot_directory + '/combined_schedule_plot_' + timestamp + '.png'}")
