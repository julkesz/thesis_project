import json
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import os
from matplotlib.patches import Rectangle, Patch

def plot_schedule_from_files(directory, output_file):
    plt.figure(figsize=(24, 16))
    fig, ax = plt.subplots()
    colors = list(mcolors.TABLEAU_COLORS.values())
    machine = 1
    bar_height = 0.4

    order_color_map = {}  # Dictionary to map order numbers to colors
    color_index = 0  # Index to cycle through colors

    machine_schedules = []
    # Iterate over all JSON files in the directory
    for filename in sorted(os.listdir(directory)):
        if filename.endswith('.json'):
            filepath = os.path.join(directory, filename)
            with open(filepath, 'r') as f:
                data = json.load(f)

            machine_schedule = []
            for index, item in enumerate(data['schedule']):
                start = item['start']
                stop = item['stop']
                num_tasks = len(item['tasks'])
                task_height = bar_height / num_tasks if num_tasks > 0 else bar_height

                for task_index, task in enumerate(item['tasks']):
                    y_pos = machine - bar_height / 2 + task_index * task_height
                    order_number = task['orderNumber']
                    task_number = task['taskId']

                    # Assign color based on order number
                    if order_number not in order_color_map:
                        order_color_map[order_number] = colors[color_index % len(colors)]
                        color_index += 1
                    color = order_color_map[order_number]

                    ax.broken_barh([(start, stop-start)], (y_pos, task_height), facecolors=color, edgecolors='black', linewidth=0.5)
                    ax.text((start + stop) / 2, y_pos + task_height / 2, f'{order_number}:{task_number}', ha='center', va='center', color='white', fontsize='x-small')

                machine_schedule.append((start, stop))

            machine_schedules.append((machine, machine_schedule))
            machine += 1

    ax.set_ylim(0.5, machine - 0.5)
    ax.set_xlim(0, max(item['stop'] for data in [json.load(open(os.path.join(directory, f))) for f in os.listdir(directory) if f.endswith('.json')] for item in data['schedule']) + 10)
    ax.set_xlabel('Czas [min]')
    ax.set_yticks(range(1, machine))
    ax.set_yticklabels([f'Drukarka {i}' for i in range(1, machine)])
    ax.grid(True, axis='x')  # Only vertical grid lines

    # Fill gaps with light grey color
    for machine, schedule in machine_schedules:
        prev_stop = 0
        for start, stop in schedule:
            if start > prev_stop:
                ax.broken_barh([(prev_stop, start-prev_stop)], (machine-bar_height/2, bar_height), facecolors='lightgrey', edgecolor='black', linewidth=0.5)
            prev_stop = stop

    # Add black border around each machine's schedule
    for machine, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        end = max(end_times)
        rect = Rectangle((start, machine - bar_height / 2), end - start, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

    # Add legend
    handles = [Patch(color=color, label=f'Zamówienie {order}') for order, color in order_color_map.items()]
    filament_patch = Patch(color='lightgrey', label='Zmiana filamentu')
    handles.append(filament_patch)
    ax.legend(handles=handles, loc='upper center', bbox_to_anchor=(0.5, -0.2))

    # Save the plot to a file
    plt.savefig(output_file, bbox_inches='tight')
    plt.close()

# Directory containing the JSON files
directory = 'src/output/'
# Output file for the plot
output_file = 'combined_schedule_plot.png'

# Plot the schedules from JSON files and save the plot to a file
plot_schedule_from_files(directory, output_file)

print(f"The plot has been saved to {output_file}")
