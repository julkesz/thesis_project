import json
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors

# Function to read JSON data
def read_json_file(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data

# Function to plot the schedule
def plot_schedule(data, output_file):
    fig, ax = plt.subplots()
    machine = 1

    # List of colors
    colors = list(mcolors.TABLEAU_COLORS.values())

    for index, item in enumerate(data['schedule']):
        start = item['start']
        stop = item['stop']
        color = colors[index % len(colors)]  # Cycle through the list of colors
        ax.broken_barh([(start, stop-start)], (machine-0.4, 0.8), facecolors=color)

        # Annotate the tasks
        for task in item['tasks']:
            ax.text((start + stop) / 2, machine, task['orderNumber'], ha='center', va='center', color='white')

    ax.set_ylim(0.5, 1.5)
    ax.set_xlim(0, max(item['stop'] for item in data['schedule']) + 10)
    ax.set_xlabel('Minutes since start')
    ax.set_yticks([machine])
    ax.set_yticklabels(['Machine'])
    ax.grid(True)

    # Save the plot to a file
    plt.savefig(output_file)
    plt.close()

# Main script
if __name__ == "__main__":
    # Path to your JSON file
    json_file_path = 'src/output/printer1schedule_20240602_230410.json'

    # Read the JSON data
    schedule_data = read_json_file(json_file_path)

    output_file = 'schedule_plot.png'
    plot_schedule(schedule_data, output_file)
