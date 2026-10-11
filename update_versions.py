import re

def is_stable(version):
    v = version.lower()
    return not any(x in v for x in ['alpha', 'beta', 'rc', 'dev', 'm', 'b', 'a', 'snapshot', 'wasm'])

with open('e:/DSU-Sideloader/versions.properties', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
current_prop_line_idx = -1
versions = []

for i, line in enumerate(lines):
    if line.startswith('version.') or line.startswith('plugin.'):
        # Apply the previous one if exists
        if current_prop_line_idx != -1 and versions:
            stable_versions = [v for v in versions if is_stable(v)]
            if stable_versions:
                latest = stable_versions[-1]
                # Check if the original version was already a stable version but maybe older, wait
                # Actually, the last stable version in the list is the latest available.
                prop_name = new_lines[current_prop_line_idx].split('=')[0]
                new_lines[current_prop_line_idx] = f"{prop_name}={latest}\n"
        
        current_prop_line_idx = len(new_lines)
        versions = []
        new_lines.append(line)
        
        # current version
        match = re.match(r'^.*?=(.*)$', line.strip())
        if match:
            versions.append(match.group(1))
            
    elif line.startswith('##') and 'available=' in line:
        match = re.search(r'available=(.*)$', line.strip())
        if match:
            versions.append(match.group(1))
        # Do not append comments to new_lines to clean up, or keep them?
        # Let's keep them
        new_lines.append(line)
    else:
        new_lines.append(line)

# Handle the last one
if current_prop_line_idx != -1 and versions:
    stable_versions = [v for v in versions if is_stable(v)]
    if stable_versions:
        latest = stable_versions[-1]
        prop_name = new_lines[current_prop_line_idx].split('=')[0]
        new_lines[current_prop_line_idx] = f"{prop_name}={latest}\n"

# Remove all the available= comments to clean up
final_lines = [line for line in new_lines if not (line.startswith('##') and 'available=' in line)]

with open('e:/DSU-Sideloader/versions.properties', 'w', encoding='utf-8') as f:
    f.writelines(final_lines)
