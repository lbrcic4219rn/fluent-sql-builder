package querybuilder.compiler;

import querybuilder.parser.Statement;

import java.util.List;

public interface ICompiler {

    CompilationResult compile(List<Statement> statements);
}
