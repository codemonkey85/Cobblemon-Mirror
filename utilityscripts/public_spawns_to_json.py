import tkinter as tk
from tkinter import filedialog, messagebox, ttk

import pandas as pd

import cobblemon_spawn_csv_to_json

groups = ["basic", "boss", "fossil"]
contexts = ["grounded", "submerged", "seafloor", "surface"]
buckets = ["common", "uncommon", "rare", "ultra-rare"]
folders = set()
file_names = set()
process_button = None


def process_file(input_file, output_directory, filters, pokemon_number_start, pokemon_number_end):
    # Filter the filters to only include options that have been toggled on
    filtered_filters = {}
    for category, options in filters.items():
        filtered_filters[category] = [name for name, var in options.items() if var.get() == 1]

    # Apply the filters to the groups, contexts, and buckets
    cobblemon_spawn_csv_to_json.ui_init_filters(pokemon_number_start, pokemon_number_end,
                                                included_grps=filtered_filters['included_groups'],
                                                known_cntxts=filtered_filters['known_contexts'],
                                                bucket_map=filtered_filters['bucket_mapping'],
                                                cstm_files=filtered_filters['file_name'],
                                                cstm_dirs=filtered_filters['folder'])

    if not validate_number_range(pokemon_number_start, pokemon_number_end):
        messagebox.showinfo("Invalid Pokémon number range. Please provide a valid range.")
        return
    cobblemon_spawn_csv_to_json.main(output_directory, input_file)


def validate_number_range(a, b):
    return 0 <= a <= b


def select_input_file(input_file_var, input_file_label, output_dir_var, pokemon_number_start_var,
                      pokemon_number_end_var, filter_vars, frame3, folder_var, file_name_var):
    global groups, contexts, buckets, process_button, folders, file_names
    filepath = filedialog.askopenfilename()
    if filepath:
        input_file_var.set(filepath)
        input_file_label.config(text=f"Selected file: {filepath}", foreground="black")
        if filepath and output_dir_var.get():
            process_button['state'] = 'normal'

        # Display a loading message
        loading_label = ttk.Label(frame3, text="Loading...")
        loading_label.grid(column=0, row=0, pady=5)

        # Disable all input options on frame3
        for child in frame3.winfo_children():
            if isinstance(child, (ttk.Button, ttk.Entry, ttk.Checkbutton, ttk.Combobox)):
                child.config(state='disabled')

        frame3.update()

        # if the file is an excel file, read it with pandas
        if input_file_var.get().endswith('.xlsx'):
            csv_df = pd.read_excel(input_file_var.get(), engine='openpyxl',
                                   dtype={'Pokémon': str, 'Entry': str, 'No.': int, 'Folder': str, 'File Name': str})
        # if the file is a csv file, read it with pandas
        elif input_file_var.get().endswith('.csv'):
            csv_df = pd.read_csv(input_file_var.get(), dtype={'Pokémon': str, 'Entry': str, 'No.': int, 'Folder': str, 'File Name': str})
        else:
            # show message
            messagebox.showinfo("Invalid file format. Please provide a valid excel or csv file")
            return
        # find all unique values in the 'group' column
        groups = csv_df['Group'].unique()
        groups = [group if group == group else '' for group in groups]
        # find all unique values in the 'context' column
        contexts = csv_df['Context'].unique()
        contexts = [context if context == context else '' for context in contexts]
        # find all unique values in the 'bucket' column
        buckets = csv_df['Bucket'].unique()
        buckets = [bucket if bucket == bucket else '' for bucket in buckets]

        # Parse the Folder and File Name columns
        folders = set(csv_df['Folder'].unique())
        folders = {folder if folder == folder else "" for folder in folders}
        folder_var.set(next(iter(folders), ""))
        file_names = set(csv_df['File Name'].unique())
        file_names = {file_name if file_name == file_name else "" for file_name in file_names}
        file_name_var.set(next(iter(file_names), ""))

        update_filter_ui(filter_vars, frame3, input_file_var, output_dir_var, pokemon_number_start_var,
                         pokemon_number_end_var, folder_var, file_name_var)

        # Remove the loading message
        loading_label.destroy()
        if output_dir_var.get() and input_file_var.get():
            process_button['state'] = 'normal'


def select_output_directory(output_dir_var, output_dir_label, input_file_var):
    global process_button
    directory = filedialog.askdirectory()
    if directory:
        output_dir_var.set(directory)
        output_dir_label.config(text=f"Output directory: {directory}", foreground="black")
        if input_file_var.get() and directory:
            process_button['state'] = 'normal'


def toggle_filter(filter_var, button):
    if filter_var.get():
        filter_var.set(False)
        button.config(style='Deactivated.TButton')
    else:
        filter_var.set(True)
        button.config(style='Active.TButton')


def create_filter_toggle_buttons(filter_category, filter_vars, start_row, parent_frame, max_columns=5):
    row, col = start_row, 0
    for name, var in filter_vars[filter_category].items():
        # Define btn before the lambda function
        btn = ttk.Button(parent_frame, text=name, style='Active.TButton')
        btn.config(command=lambda var1=var, btn1=btn: toggle_filter(var1, btn1))
        btn.grid(column=col, row=row, sticky=tk.W, pady=2, padx=2)
        col += 1
        if col >= max_columns:
            col = 0
            row += 1
    return row


def setup_ui(root):
    # Variables for storing UI inputs
    input_file_var = tk.StringVar()
    output_dir_var = tk.StringVar()
    pokemon_number_start_var = tk.IntVar(value=0)  # Default start of dex range
    pokemon_number_end_var = tk.IntVar(value=1111)  # Default end of dex range
    folder_var = tk.StringVar()
    file_name_var = tk.StringVar()

    frame = ttk.Frame(root, padding="10")
    frame.grid()
    ttk.Separator(root, orient='horizontal').grid(row=20, column=0, sticky="ew", pady=5)

    frame3 = ttk.Frame(root, padding="10")
    frame3.grid(sticky="wens")

    filter_vars, frame3 = setup_filter_ui(frame3, input_file_var, output_dir_var,
                                          pokemon_number_start_var, pokemon_number_end_var, folder_var, file_name_var)
    input_file_label, output_dir_label = setup_file_selection_ui(frame, input_file_var, output_dir_var,
                                                                 pokemon_number_start_var, pokemon_number_end_var,
                                                                 filter_vars, frame3, folder_var, file_name_var)

    # Display a warning popup to remind the user to backup their spawn.json files before they run the script
    messagebox.showwarning("Warning",
                           "Please ensure you have backed up your spawn.json files before running this script!\n"
                           "This script will OVERWRITE the spawn.json files in the specified output directory with the new data.")

    root.mainloop()


def setup_file_selection_ui(frame, input_file_var, output_dir_var, pokemon_number_start_var,
                            pokemon_number_end_var, filter_vars, frame3, folder_var, file_name_var):
    input_file_button = ttk.Button(frame, text="Select Input File",
                                   command=lambda: select_input_file(input_file_var, input_file_label,
                                                                     output_dir_var,
                                                                     pokemon_number_start_var, pokemon_number_end_var,
                                                                     filter_vars, frame3, folder_var, file_name_var))
    input_file_button.grid(column=0, row=0, pady=5)

    input_file_label = ttk.Label(frame, text="No file selected", foreground='red')
    input_file_label.grid(column=0, row=1, pady=5)

    output_dir_button = ttk.Button(frame, text="Select Output Directory",
                                   command=lambda: select_output_directory(output_dir_var, output_dir_label,
                                                                           input_file_var))
    output_dir_button.grid(column=0, row=2, pady=5)

    output_dir_label = ttk.Label(frame, text="No directory selected", foreground='red')
    output_dir_label.grid(column=0, row=3, pady=5)

    return input_file_label, output_dir_label


def update_filter_ui(filter_vars, frame3, input_file_var, output_dir_var, pokemon_number_start_var,
                     pokemon_number_end_var, folder_var, file_name_var):
    global folders, file_names
    # Clear the current filter UI
    for widget in frame3.winfo_children():
        widget.destroy()

    # Update the filter_vars based on the new unique values
    filter_vars['included_groups'] = {name: tk.IntVar(value=1) for name in groups}
    filter_vars['known_contexts'] = {name: tk.IntVar(value=1) for name in contexts}
    filter_vars['bucket_mapping'] = {name: tk.IntVar(value=1) for name in buckets}
    filter_vars['folder'] = {name: tk.IntVar(value=1) for name in folders}
    filter_vars['file_name'] = {name: tk.IntVar(value=1) for name in file_names}

    # Recreate the filter UI
    setup_filter_ui(frame3, input_file_var, output_dir_var, pokemon_number_start_var, pokemon_number_end_var, folder_var, file_name_var)


def setup_filter_ui(frame3, input_file_var, output_dir_var, pokemon_number_start_var, pokemon_number_end_var, folder_var, file_name_var):
    global process_button
    # Label and entry for specifying the range of Pokémon numbers to include
    ttk.Label(frame3, text="Pokédex Number Range:").grid(column=0, row=0, columnspan=5, pady=5)
    ttk.Entry(frame3, textvariable=pokemon_number_start_var, width=5).grid(column=1, row=1, pady=5)
    ttk.Label(frame3, text="-").grid(column=2, row=1, pady=5)
    ttk.Entry(frame3, textvariable=pokemon_number_end_var, width=5).grid(column=3, row=1, pady=5)

    filter_vars = {
        'included_groups': {name: tk.IntVar(value=1) for name in groups},
        'known_contexts': {name: tk.IntVar(value=1) for name in contexts},
        'bucket_mapping': {name: tk.IntVar(value=1) for name in buckets},
        'folder': {name: tk.IntVar(value=1) for name in folders},
        'file_name': {name: tk.IntVar(value=1) for name in file_names},
    }

    # Custom styles for toggle buttons
    style = ttk.Style()
    style.configure('Deactivated.TButton', background='gray', foreground='gray', font=('Helvetica', 10, 'overstrike'))
    style.configure('Active.TButton', background='#4CAF50', foreground='black', font=('Helvetica', 10))

    # Organize filters into sections and create toggle buttons for each
    filter_sections = [
        ('included_groups', "Groups"),
        ('known_contexts', "Contexts"),
        ('bucket_mapping', "Buckets"),
        ('folder', "Folders"),
        ('file_name', "File Names"),
    ]

    current_row = 2
    ttk.Label(frame3, text="Toggle the following filters to exclude certain "
                           "categories of cobblemon:").grid(column=0, row=current_row, columnspan=5, pady=5)
    current_row += 1
    (ttk.Label(frame3, text="An empty button means that you have some empty fields in your spreadsheet:").
     grid(column=0, row=current_row, columnspan=5, pady=5))
    current_row += 1
    for category, label_text in filter_sections:
        ttk.Label(frame3, text=f"{label_text}:").grid(column=0, row=current_row + 1, sticky=tk.W, pady=2)
        current_row += 1
        current_row = create_filter_toggle_buttons(category, filter_vars, current_row + 2, parent_frame=frame3)

    ttk.Separator(frame3, orient='horizontal').grid(row=current_row + 1, column=0, columnspan=5, sticky="ew", pady=5)
    current_row += 1

    process_button = ttk.Button(frame3, text="Execute Spawns Writer",
                                command=lambda: process_file(input_file_var.get(), output_dir_var.get(), filter_vars,
                                                             pokemon_number_start_var.get(),
                                                             pokemon_number_end_var.get()),
                                state='disabled')
    process_button.grid(column=0, row=current_row + 1, columnspan=5, sticky=(tk.W, tk.E), pady=5)

    return filter_vars, frame3


if __name__ == "__main__":
    root = tk.Tk()
    root.title("Cobblemon Spawn Data Converter")
    setup_ui(root)
