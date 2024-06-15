import json
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import os
from matplotlib.patches import Rectangle

def plot_schedule_from_files(directory, output_file):
    fig, ax = plt.subplots()
    colors = list(mcolors.TABLEAU_COLORS.values())
    machine = 1
    bar_height = 0.4

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
                color = colors[index % len(colors)]  # Cycle through the list of colors
                ax.broken_barh([(start, stop-start)], (machine-bar_height/2, bar_height), facecolors=color)

                machine_schedule.append((start, stop))

                # Annotate the tasks
                for task in item['tasks']:
                    ax.text((start + stop) / 2, machine, stop-start, ha='center', va='center', color='white')

            machine_schedules.append((machine, machine_schedule))
            machine += 1

    ax.set_ylim(0.5, machine - 0.5)
    ax.set_xlim(0, max(item['stop'] for data in [json.load(open(os.path.join(directory, f))) for f in os.listdir(directory) if f.endswith('.json')] for item in data['schedule']) + 10)
    ax.set_xlabel('Minutes since start')
    ax.set_yticks(range(1, machine))
    ax.set_yticklabels([f'Printer {i}' for i in range(1, machine)])
    ax.grid(True)

    # Add black border around each machine's schedule
    for machine, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        end = max(end_times)
        rect = Rectangle((start, machine - bar_height/2), end - start, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

    # Save the plot to a file
    plt.savefig(output_file)
    plt.close()

# Directory containing the JSON files
directory = 'src/output/'
# Output file for the plot
output_file = 'combined_schedule_plot.png'

# Plot the schedules from JSON files and save the plot to a file
plot_schedule_from_files(directory, output_file)

print(f"The plot has been saved to {output_file}")
