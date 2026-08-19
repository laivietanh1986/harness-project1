#!/usr/bin/env python3
"""Static architecture/logging conformance checker.

Enforces (see docs/ARCHITECTURE.md and CLAUDE.md):
  1. Controllers must not reference the repository layer directly (must go through a service).
  2. Controllers must not return JPA entities (must return DTOs).
  3. Every @Service class has an SLF4J Logger and every non-constructor public
     method logs at least one INFO/WARN/ERROR line (structured business-logic logging).
  4. No System.out/System.err/printStackTrace anywhere in main sources (SLF4J only).

Exits non-zero and prints one "FILE:LINE: message" violation per line if any check fails.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "src" / "main" / "java"

COMMENT_LINE_RE = re.compile(r"//.*$")


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    return "\n".join(COMMENT_LINE_RE.sub("", line) for line in text.splitlines())


def java_files(subpackage: str):
    d = SRC / "com" / "example" / "taskapi" / subpackage
    if not d.exists():
        return []
    return sorted(d.glob("*.java"))


def line_of(text: str, offset: int) -> int:
    return text.count("\n", 0, offset) + 1


class Method:
    def __init__(self, name, start_line, body):
        self.name = name
        self.start_line = start_line
        self.body = body


def extract_class_name(text: str) -> str:
    m = re.search(r"\bclass\s+(\w+)", text)
    return m.group(1) if m else ""


def extract_methods(text: str):
    """Brace-depth walk: capture blocks opened directly inside the class body (depth 0 -> 1)."""
    class_name = extract_class_name(text)
    methods = []
    depth = 0
    i = 0
    n = len(text)
    header_start = 0
    while i < n:
        ch = text[i]
        if ch == "{":
            if depth == 1:
                header = text[header_start:i]
                sig = re.search(r"(?:public|private|protected)\s+[^=;{}]*?\b(\w+)\s*\([^()]*\)\s*(?:throws\s+[\w.,\s]+)?$",
                                 header.strip())
                if sig:
                    name = sig.group(1)
                    body_start = i + 1
                    body_depth = 1
                    j = body_start
                    while j < n and body_depth > 0:
                        if text[j] == "{":
                            body_depth += 1
                        elif text[j] == "}":
                            body_depth -= 1
                        j += 1
                    body = text[body_start:j - 1]
                    if name != class_name:  # skip constructors
                        methods.append(Method(name, line_of(text, i), body))
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 1:
                header_start = i + 1
        i += 1
    return methods


def check_controllers(violations):
    for f in java_files("controller"):
        raw = f.read_text(encoding="utf-8")
        text = strip_comments(raw)
        rel = f.relative_to(ROOT)

        for m in re.finditer(r"^\s*import\s+([\w.]+)\s*;", text, re.MULTILINE):
            imp = m.group(1)
            if ".repository." in imp or imp.endswith(".repository"):
                violations.append(f"{rel}:{line_of(text, m.start())}: controller imports repository layer directly ({imp})")

        for m in re.finditer(r"\b(\w*Repository)\b\s+\w+", text):
            violations.append(f"{rel}:{line_of(text, m.start())}: controller declares a Repository-typed member/variable ({m.group(1)}) — route through a service instead")

        for m in re.finditer(r"\b(\w*[Rr]epository)\s*\.\s*\w+\s*\(", text):
            violations.append(f"{rel}:{line_of(text, m.start())}: controller calls repository method directly ({m.group(0).strip()}) — call the service instead")

        for m in re.finditer(r"\bpublic\s+(?:static\s+)?([\w.]*\bTask\b(?:<[^>]*>)?)\s+\w+\s*\(", text):
            return_type = m.group(1)
            if "TaskResponse" not in return_type and "TaskRequest" not in return_type:
                violations.append(f"{rel}:{line_of(text, m.start())}: controller method returns entity type '{return_type}' — return a DTO instead")


def check_no_raw_console_logging(violations):
    for pkg in ("controller", "service", "repository", "config", "entity", "dto"):
        for f in java_files(pkg):
            raw = f.read_text(encoding="utf-8")
            text = strip_comments(raw)
            rel = f.relative_to(ROOT)
            for m in re.finditer(r"\bSystem\.(out|err)\.\w+\s*\(", text):
                violations.append(f"{rel}:{line_of(text, m.start())}: use SLF4J Logger, not System.{m.group(1)}")
            for m in re.finditer(r"\.printStackTrace\s*\(", text):
                violations.append(f"{rel}:{line_of(text, m.start())}: use SLF4J Logger (log.error(msg, ex)), not printStackTrace()")


def check_service_logging(violations):
    for f in java_files("service"):
        raw = f.read_text(encoding="utf-8")
        text = strip_comments(raw)
        rel = f.relative_to(ROOT)

        if "@Service" not in text:
            continue

        if not re.search(r"LoggerFactory\.getLogger\s*\(", text):
            violations.append(f"{rel}:1: @Service class has no SLF4J Logger (expected `LoggerFactory.getLogger(...)`)")
            continue

        methods = extract_methods(text)
        if not methods:
            violations.append(f"{rel}:1: could not locate any methods to check logging in — verify manually")

        log_call_re = re.compile(r"\blog\.(info|warn|error)\s*\(")
        for method in methods:
            if not log_call_re.search(method.body):
                violations.append(f"{rel}:{method.start_line}: method '{method.name}' has no log.info/warn/error call — every service method must log its outcome")


def main():
    violations = []
    check_controllers(violations)
    check_no_raw_console_logging(violations)
    check_service_logging(violations)

    if violations:
        print(f"Architecture check FAILED ({len(violations)} violation(s)):\n")
        for v in violations:
            print(f"  - {v}")
        print("\nSee docs/ARCHITECTURE.md and CLAUDE.md for the rules being enforced.")
        sys.exit(1)

    print("Architecture check OK: controllers don't touch the repository layer directly, "
          "no raw console logging, all @Service methods log their outcome.")


if __name__ == "__main__":
    main()
