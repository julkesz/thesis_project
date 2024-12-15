import sys
import os
import json
import matplotlib.pyplot as plt
from matplotlib.patches import Patch, Rectangle
from matplotlib import colormaps

def plot_schedule_from_files(date_time, source_directory, output_directory, scale=1):
    fig, ax = plt.subplots(figsize=(32, 20))
    colors = [colormaps.get_cmap('tab10')(i) for i in range(20)]
    resin_colors = [colormaps.get_cmap('Set3')(i) for i in range(20)]

    machine_padding = 0.5
    bottom_margin = 0.3
    top_margin = 0.3
    bar_height = 1.0
    resin_bar_height = 0.1

    order_color_map = {}
    resin_color_map = {}
    color_index = 0
    resin_color_index = 0

    machine_schedules = []
    machine_number = 0

    for filename in sorted(os.listdir(source_directory)):
        if filename.endswith(date_time + '.json'):
            machine_schedule, machine_y_pos, color_index, resin_color_index = process_machine_schedule_with_occupancy(
                ax, filename, source_directory, colors, resin_colors, order_color_map, resin_color_map,
                color_index, resin_color_index, machine_number, scale,
                machine_padding, bottom_margin, bar_height, resin_bar_height)
            machine_schedules.append((machine_number, machine_y_pos, machine_schedule))
            machine_number += 1

    set_plot_properties(ax, machine_number, machine_schedules, source_directory, date_time, scale, bar_height, machine_padding, bottom_margin, top_margin)

    add_legends(fig, ax, order_color_map, resin_color_map)

    # Save the plot to a file
    plt.savefig(output_directory + '/schedule_' + date_time + '.png', bbox_inches='tight')
    plt.close()


def process_machine_schedule_with_occupancy(ax, filename, source_directory, colors, resin_colors, order_color_map, resin_color_map,
                                            color_index, resin_color_index, machine_number, scale,
                                            machine_padding, bottom_margin, bar_height, resin_bar_height):
    filepath = os.path.join(source_directory, filename)

    with open(filepath, 'r') as f:
        data = json.load(f)

    machine_board_size = data['boardWidth'] * data['boardLength']
    machine_schedule = []
    machine_y_pos = (machine_number+1) * bar_height + machine_number * machine_padding + bottom_margin

    for timeslot in data['schedule']:
        start = timeslot['start']
        stop = timeslot['stop']
        total_task_area = sum(task['width'] * task['length'] for task in timeslot['tasks'])
        task_heights = [task['width'] * task['length'] / machine_board_size for task in timeslot['tasks']]
        total_task_height = sum(task_heights)

        # resin handling
        resin_number = timeslot['tasks'][0]['filament']
        resin_color, resin_color_index = get_color_for_resin(resin_number, resin_colors, resin_color_map, resin_color_index)

        # resin y-position: aligned with the tasks, no space between resin and tasks
        resin_y_pos = machine_y_pos - bar_height - resin_bar_height
        resin_y_pos = round(resin_y_pos, 1)

        ax.broken_barh([(start * scale, (stop - start) * scale)], (resin_y_pos, resin_bar_height), facecolors=resin_color, edgecolors='black', linewidth=0.5)
        ax.text(((start + stop) / 2) * scale, resin_y_pos + resin_bar_height / 2, f'{resin_number}', ha='center', va='center', color='black', fontsize='x-small')

        # Task bars: stacked on top of the resin bar
        current_y_pos = machine_y_pos - bar_height
        for task_index, task in enumerate(timeslot['tasks']):
            task_height = task_heights[task_index]
            order_id = task['orderId']
            task_id = task['taskId']

            # Assign order color
            color = get_color_for_order(order_id, colors, order_color_map)

            # Plot task bar
            ax.broken_barh([(start * scale, (stop - start) * scale)], (current_y_pos, task_height), facecolors=color, edgecolors='black', linewidth=0.5)
            ax.text(((start + stop) / 2) * scale, current_y_pos + task_height / 2, f'{order_id}:{task_id}', ha='center', va='center', color='white', fontsize='small')

            current_y_pos += task_height  # Move up for the next task

        # Fill remaining space with white (if any)
        if total_task_height < bar_height:
            empty_space_height = bar_height - total_task_height
            ax.broken_barh([(start * scale, (stop - start) * scale)], (current_y_pos, empty_space_height), facecolors='white', edgecolors='black', linewidth=0.5)

        machine_schedule.append((start, stop))

    return machine_schedule, machine_y_pos, color_index, resin_color_index


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


def add_legends(fig, ax, order_color_map, resin_color_map):

    # Create order number legend in sorted order
    sorted_orders = sorted(order_color_map.keys(), key=lambda x: int(x))
    order_handles = [Patch(color=order_color_map[order], label=f'Zamówienie {order}') for order in sorted_orders]
    resin_patch = Patch(color='lightgrey', label='Zmiana materiału')

    # Create resin color legend
    resin_handles = [Patch(color=color, label=f'Materiał {resin}') for resin, color in sorted(resin_color_map.items())]

    # Increase font size and handle size for better visibility
    legend_font_size = 'large'  # You can adjust this to 'medium', 'x-large', etc.
    legend_handle_height = 2.0  # Adjust for larger handles

    # Get the position of the axes to dynamically position the legend
    box = ax.get_position()
    fig_height = box.height  # Get the height of the plot area

    # Create and position the legends dynamically based on the height of the plot
    order_legend = fig.legend(handles=order_handles + [resin_patch], loc='upper center',
                              bbox_to_anchor=(0.73, 0.07),
                              fontsize=legend_font_size, handleheight=legend_handle_height, bbox_transform=fig.transFigure)

    resin_legend = fig.legend(handles=resin_handles, loc='upper center',
                                 bbox_to_anchor=(0.8, 0.07),
                                 fontsize=legend_font_size, handleheight=legend_handle_height, bbox_transform=fig.transFigure)

    # Add legends to figure without overlap
    fig.add_artist(order_legend)  # Add the order legend separately to avoid overlap
    fig.add_artist(resin_legend)


def get_color_for_resin(resin_number, resin_colors, resin_color_map, resin_color_index):
    if resin_number not in resin_color_map:
        resin_color_map[resin_number] = resin_colors[resin_color_index % len(resin_colors)]
        resin_color_index += 1
    return resin_color_map[resin_number], resin_color_index

def get_color_for_order(order_id, colors, order_color_map):
    # Convert the order number to an integer
    order_index = int(order_id) - 1  # Subtract 1 to make it zero-based

    # Determine the color index deterministically
    color_index = order_index % len(colors)

    # Assign the color from the palette
    if order_id not in order_color_map:
        order_color_map[order_id] = colors[color_index]

    return order_color_map[order_id]



def get_max_stop_time(source_directory, date_time):
    max_stop = 0
    for filename in os.listdir(source_directory):
        if filename.endswith(date_time + '.json'):
            filepath = os.path.join(source_directory, filename)
            with open(filepath, 'r') as f:
                data = json.load(f)
            stop_times = [timeslot['stop'] for timeslot in data['schedule']]
            if len(stop_times) == 0:
                max_stop = 0
            else:
                max_stop = max(max_stop, max(stop_times))
    return max_stop


def fill_schedule_gaps(ax, schedule, machine_y_pos, bar_height, scale):
    prev_stop = 0
    for start, stop in schedule:
        if start > prev_stop:
            ax.broken_barh([(prev_stop * scale, (start - prev_stop) * scale)], (machine_y_pos - bar_height, bar_height), facecolors='lightgrey', edgecolor='black', linewidth=0.5)
        prev_stop = stop


def add_machine_borders(ax, machine_schedules, bar_height, scale):
    resin_bar_height = 0.1
    for machine_number, machine_y_pos, schedule in machine_schedules:
        end_times = [s[1] for s in schedule]
        start = 0
        if len(end_times)==0:
            end = 0
        else:
            end = max(end_times)

        # Add border for task schedule
        rect = Rectangle((start * scale, machine_y_pos - bar_height), (end - start) * scale, bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(rect)

        # Add border for resin schedule
        resin_rect = Rectangle((start * scale, machine_y_pos - bar_height - resin_bar_height), (end - start) * scale, resin_bar_height, fill=False, edgecolor='black', linewidth=1)
        ax.add_patch(resin_rect)




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

    print(f"The plot has been saved to {plot_directory + '/schedule_' + timestamp + '.png'}")
