import json
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import os
import sys
from matplotlib.patches import Rectangle, Patch


def plot_schedule_from_files(date_time, source_directory, output_directory):
    plt.figure(figsize=(32, 20))
    fig, ax = plt.subplots()
    colors = list(mcolors.TABLEAU_COLORS.values())
    filament_colors = list(mcolors.CSS4_COLORS.values())  # List of colors for filaments
    machine = 1
    bar_height = 0.8
    filament_bar_height = 0.15  # Height for filament bars

    order_color_map = {}  # Dictionary to map order numbers to colors
    filament_color_map = {}  # Dictionary to map filament numbers to colors
    color_index = 0  # Index to cycle through order colors
    filament_color_index = 0  # Index to cycle through filament colors

    machine_schedules = []
    # Iterate over all JSON files in the directory
    for filename in sorted(os.listdir(source_directory)):
        if filename.endswith(date_time + '.json'):
            filepath = os.path.join(source_directory, filename)
            with open(filepath, 'r') as f:
                data = json.load(f)

            machine_schedule = []
            for index, item in enumerate(data['schedule']):
                start = item['start']
                stop = item['stop']
                num_tasks = len(item['tasks'])
                task_height = bar_height / num_tasks if num_tasks > 0 else bar_height

                # Retrieve filament from the first task in the time slot
                first_task = item['tasks'][0]
                filament_number = first_task['filament']  # Assuming each task has a 'filament' key

                # Assign color based on filament number
                if filament_number not in filament_color_map:
                    filament_color_map[filament_number] = filament_colors[filament_color_index % len(filament_colors)]
                    filament_color_index += 1
                filament_color = filament_color_map[filament_number]

                # Plot the filament bar for the entire time slot
                filament_y_pos = machine - bar_height / 2 - filament_bar_height  # Below the entire task bar
                ax.broken_barh([(start, (stop - start)*1.2)], (filament_y_pos, filament_bar_height), facecolors=filament_color, edgecolors='black', linewidth=0.5)

                # Plot the filament number on the filament bar
                ax.text((start + stop) / 2, filament_y_pos + filament_bar_height / 2, f'{filament_number}', ha='center', va='center', color='black', fontsize='x-small')

                for task_index, task in enumerate(item['tasks']):
                    y_pos = machine - bar_height / 2 + task_index * task_height
                    order_number = task['orderNumber']
                    task_number = task['taskId']

                    # Assign color based on order number
                    if order_number not in order_color_map:
                        order_color_map[order_number] = colors[color_index % len(colors)]
                        color_index += 1
                    color = order_color_map[order_number]

                    # Plot the task execution bar
                    ax.broken_barh([(start, (stop - start)*1.2)], (y_pos, task_height), facecolors=color, edgecolors='black', linewidth=0.5)

                    # Plot the task number (order number + task number)
                    ax.text((start + stop) / 2, y_pos + task_height / 2, f'{order_number}:{task_number}', ha='center', va='center', color='white', fontsize='x-small')

                machine_schedule.append((start, stop))

            machine_schedules.append((machine, machine_schedule))
            machine += 1
    # Set limits for axes
    ax.set_ylim(0.5, machine - 0.5)
    ax.set_xlim(0, max(item['stop'] for data in [json.load(open(os.path.join(source_directory, f))) for f in os.listdir(source_directory) if f.endswith('.json')] for item in data['schedule']) * 1.2 + 20)

    ax.set_xlabel('Czas [min]')
    ax.set_yticks(range(1, machine))
    ax.set_yticklabels([f'Drukarka {i}' for i in range(1, machine)])
    ax.grid(True, axis='x')  # Only vertical grid lines

    # Fill gaps with light grey color
    for machine, schedule in machine_schedules:
        prev_stop = 0
        for start, stop in schedule:
            if start > prev_stop:
                ax.broken_barh([(prev_stop, (start - prev_stop)*1.2)], (machine - bar_height / 2, bar_height), facecolors='lightgrey', edgecolor='black', linewidth=0.5)
            prev_stop = stop

    # Add black border around each machine's schedule and filament bar
    for machine, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        end = max(end_times)

        # Add black border around the task bars (existing)
        rect = Rectangle((start, machine - bar_height / 2), (end - start)*1.2, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

        # Add black border around the filament bar for each machine
        filament_rect = Rectangle((start, machine - bar_height / 2 - filament_bar_height), (end - start)*1.2, filament_bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(filament_rect)

    # Add legend for order numbers and filament replacement
    order_handles = [Patch(color=color, label=f'Zamówienie {order}') for order, color in order_color_map.items()]
    filament_patch = Patch(color='lightgrey', label='Zmiana filamentu')

    # Add legend for filament colors
    filament_handles = [Patch(color=color, label=f'Filament {filament}') for filament, color in sorted(filament_color_map.items())]

    # Create and position the legends
    fig.legend(handles=order_handles + [filament_patch], loc='upper left', bbox_to_anchor=(0.1, -0.1), bbox_transform=fig.transFigure)
    fig.legend(handles=filament_handles, loc='upper left', bbox_to_anchor=(0.6, -0.1), bbox_transform=fig.transFigure)

    # Save the plot to a file
    plt.savefig(output_directory + '/combined_schedule_plot_' + date_time + '.png', bbox_inches='tight')
    plt.close()


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

