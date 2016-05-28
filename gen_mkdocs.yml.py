import os
import re

modules_dir = 'src/modules'
manuals_dir = 'src/manual'

# return the inner-XML of a certain XML-tag
def extract_xml_value(tag_name, data):
    matches = re.search("<{}>(.*)</{}>".format(tag_name, tag_name), data)
    return matches.group(1)

def extract_h1(filename):
    with open(filename, 'r') as md_file:
        data = md_file.read()
    matches = re.search("^# (.*)\n$", data)
    return matches.group(1)

# return the list of .md files in a directory
def list_markdown_files(dir):
    mds = {}
    for item in os.listdir(dir):
        if item.endswith(".md"):
            mds[item] = "{}/{}".format(dir, item)
    return mds;

def make_manuals_list(dir):
    mds = []
    for item in os.listdir(dir):
        if item.endswith(".md"):
            filename = "{}/{}".format(dir, item)
            title = extract_h1(filename)
            mds.append((title, filename))
    return mds;

def make_subdoc_yaml(list):
    out = []
    for (title, filename) in list:
        out.append("    - '{}' : '{}'".format(title, filename))
    return out

# search modules and return a list sorted by module name
def make_module_list():
    module_list = []
    for subdir in os.listdir(modules_dir):
        module_dir = "{}/{}".format(modules_dir, subdir)

        # extract information form POM
        with open("{}/{}".format(module_dir, 'pom.xml'), 'r') as pom_file:
            pom = pom_file.read()
        module_name = extract_xml_value('name', pom)
        module_list.append( (module_dir, module_name) )

    return sorted(module_list, key=lambda x: x[1])

#
#    MAIN
#
module_docs = []        # list of module manual pages
command_docs = []       # list of module command pages
config_docs = []        # list of module configuration pages
manual_docs = make_manuals_list(manuals_dir)
nodoc_modules = []      # list of modules that lack documentation

# search all modules for documentation
modules = make_module_list();       # get list of all modules
for m in modules:
    doc_dir = "{}/src/main/resources/manual".format(m[0])
    if (os.path.isdir(doc_dir)):
        docs = list_markdown_files(doc_dir)
        if 'module.md' in docs.keys(): module_docs.append( (m[1], docs['module.md']) )
        if 'config.md' in docs.keys(): config_docs.append( (m[1], docs['config.md']) )
        if 'commands.md' in docs.keys(): command_docs.append( (m[1], docs['commands.md']) )
    else:
        nodoc_modules.append(m)

# generate MkDocs YAML
with open('mkdocs.yml', 'w') as mkdocs:
    mkdocs.write("site_name: LectureSight Doc\n")
    mkdocs.write("docs_dir: .\n")
    mkdocs.write("pages:\n")
    mkdocs.write("- Home: index.md\n")
    mkdocs.write("- Manuals:\n")
    mkdocs.write('\n'.join(make_subdoc_yaml(manual_docs)) + '\n')
    mkdocs.write("- Modules:\n")
    mkdocs.write('\n'.join(make_subdoc_yaml(module_docs)) + '\n')
    mkdocs.write("- Commands:\n")
    mkdocs.write('\n'.join(make_subdoc_yaml(command_docs)) + '\n')
    mkdocs.write("- Configs:\n")
    mkdocs.write('\n'.join(make_subdoc_yaml(config_docs)) + '\n')
