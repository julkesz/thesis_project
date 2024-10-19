
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
    machine = 0

    for filename in sorted(os.listdir(source_directory)):
        if filename.endswith(date_time + '.json'):
            machine_schedule, color_index, filament_color_index = process_machine_schedule_with_occupancy(
                ax, filename, source_directory, colors, filament_colors, order_color_map, filament_color_map,
                color_index, filament_color_index, machine, scale, machine_board_size
            )
            machine_schedules.append((machine, machine_schedule))
            machine += 1

    set_plot_properties(ax, machine, machine_schedules, source_directory, date_time, scale, bar_height=1.0)  # Default height is now 1 (machine fully occupied)

    add_legends(fig, ax, order_color_map, filament_color_map)

    # Save the plot to a file
    plt.savefig(output_directory + '/combined_schedule_plot_' + date_time + '.png', bbox_inches='tight')
    plt.close()

def process_machine_schedule_with_occupancy(ax, filename, source_directory, colors, filament_colors, order_color_map, filament_color_map,
                                            color_index, filament_color_index, machine, scale, machine_board_size, machine_padding=0.5):
    filepath = os.path.join(source_directory, filename)

    with open(filepath, 'r') as f:
        data = json.load(f)

    bar_height = 1.0  # Maintain the same height for the task bar (tasks and filament bars should stay together)
    filament_bar_height = 0.1  # Keep the filament bar smaller than the task bar
    machine_schedule = []
    baseline_offset = 0.3

    # Compute base Y position for this machine, leaving space between machines
    machine_offset = (machine+1) * bar_height + machine * machine_padding + baseline_offset

    for item in data['schedule']:
        start = item['start']
        stop = item['stop']
        total_task_area = sum(task['width'] * task['length'] for task in item['tasks'])
        task_heights = [task['width'] * task['length'] / machine_board_size for task in item['tasks']]
        total_task_height = sum(task_heights)

        # Filament handling
        filament_number = item['tasks'][0]['filament']
        filament_color, filament_color_index = get_color_for_filament(filament_number, filament_colors, filament_color_map, filament_color_index)

        # Filament y-position: aligned with the tasks, no space between filament and tasks
        filament_y_pos = machine_offset - bar_height - filament_bar_height
        filament_y_pos = round(filament_y_pos, 1)
        #print(" Machine offset for machine ", machine, ": ", machine_offset)
        #print(" Filament ypos for machine ", machine, ": ", filament_y_pos)
        ax.broken_barh([(start * scale, (stop - start) * scale)], (filament_y_pos, filament_bar_height), facecolors=filament_color, edgecolors='black', linewidth=0.5)
        ax.text(((start + stop) / 2) * scale, filament_y_pos + filament_bar_height / 2, f'{filament_number}', ha='center', va='center', color='black', fontsize='x-small')

        # Task bars: stacked on top of the filament bar
        current_y_pos = machine_offset - bar_height
        for task_index, task in enumerate(item['tasks']):
            task_height = task_heights[task_index]
            order_number = task['orderNumber']
            task_number = task['taskId']

            # Assign order color
            color, color_index = get_color_for_order(order_number, colors, order_color_map, color_index)

            # Plot task bar
            ax.broken_barh([(start * scale, (stop - start) * scale)], (current_y_pos, task_height), facecolors=color, edgecolors='black', linewidth=0.5)
            ax.text(((start + stop) / 2) * scale, current_y_pos + task_height / 2, f'{order_number}:{task_number}', ha='center', va='center', color='white', fontsize='small')

            current_y_pos += task_height  # Move up for the next task

        # Fill remaining space with white (if any)
        if total_task_height < bar_height:
            empty_space_height = bar_height - total_task_height
            ax.broken_barh([(start * scale, (stop - start) * scale)], (current_y_pos, empty_space_height), facecolors='white', edgecolors='black', linewidth=0.5)

        machine_schedule.append((start, stop))

    return machine_schedule, color_index, filament_color_index

def set_plot_properties(ax, machine, machine_schedules, source_directory, date_time, scale, bar_height, machine_padding=0.5):
    # Set limits for y-axis: calculate the total height based on the number of machines
    total_height = machine * bar_height + (machine-1) * machine_padding + 0.3 + 0.3 #baseline_offset, top_offset
    ax.set_ylim(0, total_height)  # Set ylim to the exact height needed for all machines

    #print("Total height: ", total_height)
    # Set x-axis limit based on the maximum stop time
    ax.set_xlim(0, get_max_stop_time(source_directory, date_time) * scale + 20)
    #print("MACHINE: ", machine)
    machine_offsets = [(i+1) * bar_height + i * machine_padding + 0.3 for i in range(machine)] #baseline_offset
    # Calculate y-tick positions: place the label at the vertical midpoint of each machine schedule
    ytick_positions = [machine_offsets[j] - bar_height / 2 for j in range(machine)]
    #print("yticks: ", ytick_positions)
    ax.set_yticks(ytick_positions)
    ax.set_yticklabels([f'Drukarka {i}' for i in range(1, machine + 1)])

    # Add grid lines
    ax.grid(True, axis='x')
    #print("Machine schedules:", machine_schedules)
    baseline_offset = 0.3


    # Fill gaps in the schedule
    for machine_num, schedule in machine_schedules:
        machine_offset = (machine_num+1) * bar_height + machine_num * machine_padding + baseline_offset
        fill_schedule_gaps(ax, schedule, machine_num, machine_offset, bar_height, scale)

    # Add machine borders
    add_machine_borders(ax, machine_schedules, bar_height, scale, machine_padding)




def add_legends(fig, ax, order_color_map, filament_color_map):
    # Create order number legend
    order_handles = [Patch(color=color, label=f'Zamówienie {order}') for order, color in order_color_map.items()]
    filament_patch = Patch(color='lightgrey', label='Zmiana filamentu')

    # Create filament color legend
    filament_handles = [Patch(color=color, label=f'Filament {filament}') for filament, color in sorted(filament_color_map.items())]

    # Increase font size and handle size for better visibility
    legend_font_size = 'large'  # You can adjust this to 'medium', 'x-large', etc.
    legend_handle_height = 2.0  # Adjust for larger handles

    # Get the position of the axes to dynamically position the legend
    box = ax.get_position()
    fig_height = box.height  # Get the height of the plot area

    # Create and position the legends dynamically based on the height of the plot
    order_legend = fig.legend(handles=order_handles + [filament_patch], loc='upper center',
                              bbox_to_anchor=(0.73, 0.07),
                              fontsize=legend_font_size, handleheight=legend_handle_height, bbox_transform=fig.transFigure)

    filament_legend = fig.legend(handles=filament_handles, loc='upper center',
                                 bbox_to_anchor=(0.8, 0.07),
                                 fontsize=legend_font_size, handleheight=legend_handle_height, bbox_transform=fig.transFigure)

    # Add legends to figure without overlap
    fig.add_artist(order_legend)  # Add the order legend separately to avoid overlap
    fig.add_artist(filament_legend)


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

def fill_schedule_gaps(ax, schedule, machine_num, machine_offset, bar_height, scale):
    prev_stop = 0
    for start, stop in schedule:
        if start > prev_stop:
            #print("Machine offset for ", machine_num, ":  ", machine_offset)
            ax.broken_barh([(prev_stop * scale, (start - prev_stop) * scale)], (machine_offset - bar_height, bar_height), facecolors='lightgrey', edgecolor='black', linewidth=0.5)
        prev_stop = stop

def add_machine_borders(ax, machine_schedules, bar_height, scale, machine_padding=0.5):
    filament_bar_height = 0.1
    for machine, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        end = max(end_times)

        # Calculate machine offset for correct border positioning
        baseline_offset = 0.3

        machine_offset = ((machine+1) * bar_height) + (machine * machine_padding) + baseline_offset

        #print("Machine offset for machine ", machine, ":        ", machine_offset)
        #print("Machine bar_height for machine ", machine, ":        ", bar_height)
        #print("Machine machine_padding for machine ", machine, ":        ", bar_height)


        # Add border for task schedule
        rect = Rectangle((start * scale, machine_offset - bar_height), (end - start) * scale, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

        # Add border for filament schedule
        filament_rect = Rectangle((start * scale, machine_offset - bar_height - filament_bar_height), (end - start) * scale, filament_bar_height, fill=False, edgecolor='black', linewidth=1)
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
