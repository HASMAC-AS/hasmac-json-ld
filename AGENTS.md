# Build Instructions

To build the project, use the Maven wrapper in offline mode:

```bash
./mvnw -o clean package
```

# Running Tests

Execute the full test suite (also in offline mode) with:

```bash
./mvnw -o verify
```

# Modification Guidelines
Codex must not modify the Maven wrapper scripts or any files inside the `.mvn` directory. Specifically, avoid editing:
- `mvnw`
- `mvnw.cmd`
- `.mvn/` and all of its contents
