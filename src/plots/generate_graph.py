
import sys
import os
import json
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from matplotlib.patches import Patch, Rectangle


def plot_schedule_from_files(date_time, source_directory, output_directory, scale=1.2, machine_board_size=6000):
    fig, ax = plt.subplots(figsize=(32, 20))
    colors = list(mcolors.TABLEAU_COLORS.values())
    filament_colors = list(mcolors.CSS4_COLORS.values())

    machine_padding = 0.5
    bottom_margin = 0.3
    top_margin = 0.3
    bar_height = 1.0
    filament_bar_height = 0.1

    order_color_map = {}
    filament_color_map = {}
    color_index = 0
    filament_color_index = 0

    machine_schedules = []
    machine_number = 0

    for filename in sorted(os.listdir(source_directory)):
        if filename.endswith(date_time + '.json'):
            machine_schedule, machine_y_pos, color_index, filament_color_index = process_machine_schedule_with_occupancy(
                ax, filename, source_directory, colors, filament_colors, order_color_map, filament_color_map,
                color_index, filament_color_index, machine_number, scale, machine_board_size,
                machine_padding, bottom_margin, bar_height, filament_bar_height)
            machine_schedules.append((machine_number, machine_y_pos, machine_schedule))
            machine_number += 1

    set_plot_properties(ax, machine_number, machine_schedules, source_directory, date_time, scale, bar_height, machine_padding, bottom_margin, top_margin)

    add_legends(fig, ax, order_color_map, filament_color_map)

    # Save the plot to a file
    plt.savefig(output_directory + '/combined_schedule_plot_' + date_time + '.png', bbox_inches='tight')
    plt.close()


def process_machine_schedule_with_occupancy(ax, filename, source_directory, colors, filament_colors, order_color_map, filament_color_map,
                                            color_index, filament_color_index, machine_number, scale, machine_board_size,
                                            machine_padding, bottom_margin, bar_height, filament_bar_height):
    filepath = os.path.join(source_directory, filename)

    with open(filepath, 'r') as f:
        data = json.load(f)

    machine_schedule = []

    machine_y_pos = (machine_number+1) * bar_height + machine_number * machine_padding + bottom_margin

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
        filament_y_pos = machine_y_pos - bar_height - filament_bar_height
        filament_y_pos = round(filament_y_pos, 1)

        ax.broken_barh([(start * scale, (stop - start) * scale)], (filament_y_pos, filament_bar_height), facecolors=filament_color, edgecolors='black', linewidth=0.5)
        ax.text(((start + stop) / 2) * scale, filament_y_pos + filament_bar_height / 2, f'{filament_number}', ha='center', va='center', color='black', fontsize='x-small')

        # Task bars: stacked on top of the filament bar
        current_y_pos = machine_y_pos - bar_height
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

    return machine_schedule, machine_y_pos, color_index, filament_color_index


def set_plot_properties(ax, machine_number, machine_schedules, source_directory, date_time, scale, bar_height, machine_padding, bottom_margin, top_margin):
    total_height = machine_number * bar_height + (machine_number-1) * machine_padding + bottom_margin + top_margin
    ax.set_ylim(0, total_height)

    ax.set_xlim(0, get_max_stop_time(source_directory, date_time) * scale + 20)
    machine_y_positions = [x[1] for x in machine_schedules]
    ytick_positions = [machine_y_positions[j] - bar_height / 2 for j in range(machine_number)]

    ax.set_yticks(ytick_positions)
    ax.set_yticklabels([f'Drukarka {i}' for i in range(1, machine_number + 1)])

    # Add grid lines
    ax.grid(True, axis='x')

    # Fill gaps in the schedule
    for machine_num, machine_y_pos, schedule in machine_schedules:
        fill_schedule_gaps(ax, schedule, machine_y_pos, bar_height, scale)

    # Add machine borders
    add_machine_borders(ax, machine_schedules, bar_height, scale)


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


def fill_schedule_gaps(ax, schedule, machine_y_pos, bar_height, scale):
    prev_stop = 0
    for start, stop in schedule:
        if start > prev_stop:
            ax.broken_barh([(prev_stop * scale, (start - prev_stop) * scale)], (machine_y_pos - bar_height, bar_height), facecolors='lightgrey', edgecolor='black', linewidth=0.5)
        prev_stop = stop


def add_machine_borders(ax, machine_schedules, bar_height, scale):
    filament_bar_height = 0.1
    for machine_number, machine_y_pos, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        end = max(end_times)

        # Add border for task schedule
        rect = Rectangle((start * scale, machine_y_pos - bar_height), (end - start) * scale, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

        # Add border for filament schedule
        filament_rect = Rectangle((start * scale, machine_y_pos - bar_height - filament_bar_height), (end - start) * scale, filament_bar_height, fill=False, edgecolor='black', linewidth=1)
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
