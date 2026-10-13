#!/usr/bin/env python3
"""Paste blockbench/generated/<name>_mesh.java.txt into the model class between its GENERATED markers, and set
the LayerDefinition.create(mesh, W, H) sizes to the declared texture size on the file's first line.

    python3 tools/blockbench/install_mesh.py giraffe GiraffeModel
"""
import os, re, sys

ROOT = os.path.join(os.path.dirname(__file__), "..", "..")
name, cls = sys.argv[1], sys.argv[2]
gen = open(os.path.join(ROOT, "blockbench", "generated", name + "_mesh.java.txt")).read()
header, mesh = gen.split("\n", 1)
w, h = re.search(r"create\(mesh, (\d+), (\d+)\)", header).groups()
path = os.path.join(ROOT, "src", "main", "java", "com", "sablednah", "cityworldzoo", "client", "model", cls + ".java")
s = open(path).read()
start = s.index("        PartDefinition root = mesh.getRoot();\n") + len("        PartDefinition root = mesh.getRoot();\n")
end = s.index("        return mesh;\n    }\n    // --- end generated ---")
s = s[:start] + mesh + s[end:]
s = re.sub(r"LayerDefinition\.create\(mesh\(\), \d+, \d+\)", f"LayerDefinition.create(mesh(), {w}, {h})", s)
open(path, "w").write(s)
print(f"{cls}: mesh installed, texture declared {w}x{h}")
