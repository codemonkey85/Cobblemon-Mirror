import shutil
import textwrap

cobblemonheader = """
        █████████           █████     █████     ████                                              
       ███░░░░░███         ░░███     ░░███     ░░███                                              
      ███     ░░░   ██████  ░███████  ░███████  ░███   ██████  █████████████    ██████  ████████  
     ░███          ███░░███ ░███░░███ ░███░░███ ░███  ███░░███░░███░░███░░███  ███░░███░░███░░███ 
     ░███         ░███ ░███ ░███ ░███ ░███ ░███ ░███ ░███████  ░███ ░███ ░███ ░███ ░███ ░███ ░███ 
     ░░███     ███░███ ░███ ░███ ░███ ░███ ░███ ░███ ░███░░░   ░███ ░███ ░███ ░███ ░███ ░███ ░███ 
      ░░█████████ ░░██████  ████████  ████████  █████░░██████  █████░███ █████░░██████  ████ █████
       ░░░░░░░░░   ░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░  ░░░░░░  ░░░░░ ░░░ ░░░░░  ░░░░░░  ░░░░ ░░░░░ 
"""


def printShadowedCobblemonHeader():
    print(cobblemonheader)


def printCobblemonHeader():
    # Replace the shaded characters with spaces
    replaced = cobblemonheader.replace("░", " ")
    # Split the string into lines
    lines = replaced.splitlines()
    # Remove the first character of each line
    lines = [line[2:] for line in lines]
    # Join the lines back together into a single string
    result = '\n'.join(lines)
    print(result)


def print_cobblemon_script_description(header, body=""):
    # Define the width of the box
    width = 96
    # Create the top border
    print("╔─" + "─" * width + "─╗")
    # Print the header
    print("│ " + header.center(width) + " │")
    if body != "":
        # Create a separator
        print("│─" + "─" * width + "─│")
        # Wrap the body
        wrapped_body = textwrap.wrap(body, width)
        # Print each line of the body
        for line in wrapped_body:
            print("│ " + line.ljust(width) + " │")
    # Create the bottom border
    print("╚─" + "─" * width + "─╝")


def print_cobblemon_script_footer(text):
    print_cobblemon_script_description(text)


def print_list_filtered(lines, filter_words=None):
    for line in lines:
        if filter_words is None or not line.startswith(filter_words):
            print("  " + line)


def print_warning(message):
    print()
    print("⚠️ " + message + " ⚠️")


def print_problems_and_paths(problem_path_tuples, filter_words=None):
    # Calculate the maximum problem description length
    max_width = max(len(problem) for problem, _ in problem_path_tuples)
    for problem, path in problem_path_tuples:
        # Only print the tuple if the problem description does not contain the filter_words
        if filter_words is None or filter_words not in problem:
            print("  {:<{}} {}".format(problem, max_width, path))

# Print a separator line as wide as the width of the box
def print_separator():
    # get the width of the terminal
    width = shutil.get_terminal_size()
    print("\n")
    print("─" * width.columns)
    print("\n")




def sanitize_pokemon(pokemon):
    return (pokemon.replace("-", "").replace("♂", "m").replace("♀", "f")
            .replace(".", "").replace("'", "").replace(' ', '')
            .replace('é', 'e').replace(':', '').replace('’', '').replace('é', 'e').lower())
