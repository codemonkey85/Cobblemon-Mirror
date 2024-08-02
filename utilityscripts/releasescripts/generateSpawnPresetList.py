import json
import os


def generate_markdown(json_directory, output_markdown_file):
    json_files = [f for f in os.listdir(json_directory) if f.endswith('.json')]

    # Define the header
    header = "# Spawn Presets\n\nThis file contains all current spawn presets of cobblemon.\n\n"

    # Use GitLab flavored markdown's [TOC] for generating the table of contents
    toc = "[[_TOC_]]\n\n# Spawn Preset List\n\n"

    markdown_content = header + toc

    for file_name in json_files:
        file_path = os.path.join(json_directory, file_name)
        with open(file_path, 'r') as json_file:
            data = json.load(json_file)

        # Extract the root name from the file_name for the markdown section title
        root_name = file_name.split('.')[0]
        markdown_content += f"## {root_name}\n\n"

        # Update the table header to include the new column
        markdown_content += "| Key | Conditions | Anticonditions |\n"
        markdown_content += "| --- | ---------- | -------------- |\n"

        conditions = data.get("condition", {})
        anticonditions = data.get("anticondition", {})

        # Collect all unique keys from conditions and anticonditions
        all_keys = set(conditions.keys()) | set(anticonditions.keys())

        # Collect all rows in a list
        rows = []
        for key in all_keys:
            condition_values = conditions.get(key, [])
            anticondition_values = anticonditions.get(key, [])

            # Ensure values are list for uniform processing
            if not isinstance(condition_values, list):
                condition_values = [condition_values]
            if not isinstance(anticondition_values, list):
                anticondition_values = [anticondition_values]

            # Add a third column at the front and put the key there in bold
            row = f"| **{key}** | {'; '.join([f'`{v}`' for v in condition_values])} | {'; '.join([f'`{v}`' for v in anticondition_values])} |\n"
            rows.append((key, row))

        # Sort the rows alphabetically by key and add them to the markdown content
        for _, row in sorted(rows):
            markdown_content += row

    # Write markdown content to the specified output file
    with open(output_markdown_file, 'w') as output_file:
        output_file.write(markdown_content)


# Example usage
json_directory = '../../common/src/main/resources/data/cobblemon/spawn_detail_presets'
output_markdown_file = '../../docs/cobblemon-tags/spawnPresetList.md'
generate_markdown(json_directory, output_markdown_file)