package Scan;

import AST.*;

import static Scan.TokenKind.*;

public class ParserAST
{
	private Scanner scan;
	private Token currentTerminal;

	public ParserAST(Scanner scan)
	{
		this.scan = scan;

		currentTerminal = scan.scan();
	}

	public Program parseProgram()
	{
		accept(START);
		Block block = parseBlock();
		accept(END);
		if(currentTerminal.kind != EOT)
			System.out.println("Tokens found after end of program");
		return new Program(block);
	}

	private Block parseBlock()
	{
		Declarations decs = new Declarations();
		Statements stats = new Statements();
		parseDeclarations(decs);
		parseStatements();
		return new Block(decs, stats);
	}

	private Declarations parseDeclarations(Declarations decs)
	{
		while(currentTerminal.kind == INT || currentTerminal.kind == BOOL || currentTerminal.kind == ARRAY || currentTerminal.kind == DEFINE)
			decs.dec.add(parseOneDeclaration());

		return decs;
	}

	private Identifier parseIdentifier()
	{
		if(currentTerminal.kind == IDENTIFIER)
		{
			Identifier res = new Identifier(currentTerminal.spelling);
			currentTerminal = scan.scan();

			return res;
		}
		else
		{
			System.out.println("Identifier expected");

			return new Identifier("???");
		}
	}

	private TypeValue parseType()
	{
		if(currentTerminal.kind == ARRAY)
		{
			TypeValue typeValueClass = new TypeValue(TypeValue.TypeName.ARR);
			currentTerminal = scan.scan();
			System.out.println(typeValueClass.type);
			return typeValueClass;
		}
		else if(currentTerminal.kind == INT)
		{
			TypeValue typeValueClass = new TypeValue(TypeValue.TypeName.INT);
			currentTerminal = scan.scan();
			System.out.println(typeValueClass.type);
			return typeValueClass;
		}
		else if(currentTerminal.kind == BOOL)
		{
			TypeValue typeValueClass = new TypeValue(TypeValue.TypeName.BOOL);
			currentTerminal = scan.scan();
			System.out.println(typeValueClass.type);
			return typeValueClass;
		}
		else if(currentTerminal.kind == NULL)
		{
			TypeValue typeValueClass = new TypeValue(TypeValue.TypeName.NULL);
			currentTerminal = scan.scan();

			System.out.println(typeValueClass.type);
			return typeValueClass;
		}
		else
		{
			System.out.println("Type expected");

			return new TypeValue(TypeValue.TypeName.ERROR);
		}
	}

	private IntegerLiteral parseIntegerLiteral()
	{
		if(currentTerminal.kind == INTEGERLITERAL)
		{
			IntegerLiteral res = new IntegerLiteral(currentTerminal.spelling);
			currentTerminal = scan.scan();

			return res;
		}
		else
		{
			System.out.println("Integer literal expected");

			return new IntegerLiteral("???");
		}
	}

	private Statements parseArrayDeclaration()
	{
		Statements list = new Statements();

		list.stat.add(new ExpressionStatement(new IntLitExpression(parseIntegerLiteral())));

		while(currentTerminal.kind == COMMA)
		{
			accept(COMMA);
			list.stat.add(new ExpressionStatement(new IntLitExpression(parseIntegerLiteral())));
		}

		return list;
	}

	private Declaration parseOneDeclaration()
	{
		Statement stat = null;
		IntegerLiteral lit;
		TypeValue typeValueClass1;
		TypeValue typeValueClass2;
		Identifier id;
		Expression retExp;
		Block block;
		switch(currentTerminal.kind)
		{
			case BOOL:
			case INT:
				typeValueClass1 = parseType();
				id = parseIdentifier();
				if(currentTerminal.kind != SEMICOLON)
					stat = parseOneStatement();
				else
					accept(SEMICOLON);
				if(stat == null)
					return new VariableDeclaration(typeValueClass1, id);
				else
					return new VariableDeclaration(typeValueClass1, id, stat);

			case ARRAY:
				typeValueClass1 = parseType();
				id = parseIdentifier();
				accept(LEFTARROW);
				if(currentTerminal.kind == INT)
					typeValueClass2 = parseType();
				else if(currentTerminal.kind == BOOL)
					typeValueClass2 = parseType();
				else
					typeValueClass2 = new TypeValue(TypeValue.TypeName.ERROR);
				accept(RIGHTARROW);
				accept(LEFTPARAN);
				lit = parseIntegerLiteral();
				accept(RIGHTPARAN);
				Statements stat2 = new Statements();
				if(currentTerminal.kind != SEMICOLON)
				{
					accept(OPERATOR);
					accept(LEFTPARAN);
					stat2 = parseArrayDeclaration();
					accept(RIGHTPARAN);
					accept(SEMICOLON);
				}
				else
					accept(SEMICOLON);
				if(stat == null)
					return new VariableDeclaration(typeValueClass1, id);
				else
					return new VariableDeclaration(typeValueClass1, typeValueClass2, lit, id, stat2);

			case DEFINE:
				accept(DEFINE);
				typeValueClass1 = parseType();
				id = parseIdentifier();
				accept(LEFTSQUARE);
				Declarations params = null;
				if(currentTerminal.kind == INT || currentTerminal.kind == BOOL || currentTerminal.kind == ARRAY)
				{
					params = parseParams();
				}
				accept(RIGHTSQUARE);
				accept(LEFTARROW);
				block = parseBlock();
				if(currentTerminal.kind == RETURN)
				{
					accept(RETURN);
					retExp = parseExpression();
					accept(SEMICOLON);
					accept(RIGHTARROW);
					return new FunctionDeclaration(typeValueClass1, id, params, block, retExp);
				}
				accept(RIGHTARROW);
				return new FunctionDeclaration(typeValueClass1, id, params, block);

			default:
				System.out.println("Declaration or statement expected.");
				return null;
		}
	}

	private Declarations parseParams()
	{
		Declarations list = new Declarations();

		list.dec.add(new VariableDeclaration(parseType(), parseIdentifier()));

		while(currentTerminal.kind == COMMA)
		{
			accept(COMMA);
			list.dec.add(new VariableDeclaration(parseType(), parseIdentifier()));
		}

		return list;
	}

	private ExpList parseExpList()
	{
		ExpList list = new ExpList();
		Expression exp = parseExpression();
		list.exp.add(exp);
		while(currentTerminal.kind == COMMA)
		{
			accept(COMMA);
			exp = parseExpression();
			list.exp.add(exp);
		}

		return list;
	}

	private Statements parseStatements()
	{
		Statements stats = new Statements();
		while(currentTerminal.kind == IDENTIFIER || currentTerminal.kind == OPERATOR || currentTerminal.kind == INTEGERLITERAL || currentTerminal.kind == IF || currentTerminal.kind == WHILE || currentTerminal.kind == PRINT || currentTerminal.kind == INPUT)
			stats.stat.add(parseOneStatement());

		return stats;
	}

	private Statement parseOneStatement()
	{
		switch(currentTerminal.kind)
		{
			case IDENTIFIER:
			case INTEGERLITERAL:
			case OPERATOR:
				Expression exp = parseExpression();
				accept(SEMICOLON);
				return new ExpressionStatement(exp);
			case IF:
				accept(IF);
				accept(LEFTSQUARE);
				Expression ifExp = parseExpression();
				accept(RIGHTSQUARE);
				accept(LEFTARROW);
				Statements thenPart = parseStatements();
				accept(RIGHTARROW);

				Statements elsePart = null;
				if(currentTerminal.kind == ELSE)
				{
					accept(ELSE);
					accept(LEFTARROW);
					elsePart = parseStatements();
					accept(RIGHTARROW);
				}

				return new IfStatement(ifExp, thenPart, elsePart);

			case WHILE:
				accept(WHILE);
				accept(LEFTSQUARE);
				Expression whileExp = parseExpression();
				accept(RIGHTSQUARE);
				accept(LEFTARROW);
				Statements stats = parseStatements();
				accept(RIGHTARROW);
				return new WhileStatement(whileExp, stats);

			case PRINT:
				accept(PRINT);
				accept(LEFTSQUARE);
				Expression printExp = parseExpression();
				accept(RIGHTSQUARE);
				accept(SEMICOLON);
				return new PrintStatement(printExp);

			case INPUT:
				accept(INPUT);
				accept(LEFTSQUARE);
				TypeValue typeValueClass = parseType();
				accept(COMMA);
				Identifier idf = parseIdentifier();
				accept(RIGHTSQUARE);
				accept(SEMICOLON);
				return new InputStatement(typeValueClass, idf);

			default:
				System.out.println("Error in statement");
				return null;
		}
	}

	private Expression parseExpression()
	{
		Expression res = parseExpression1();
		while(currentTerminal.isAssignOperator())
		{
			Operator op = parseOperator();
			Expression tmp = parseExpression1();

			res = new BinaryExpression(op, res, tmp);
		}
		return res;
	}

	private Expression parseExpression1()
	{
		Expression res = parseExpression2();
		while( currentTerminal.isAddOperator() ) {
			Operator op = parseOperator();
			Expression tmp = parseExpression2();

			res = new BinaryExpression( op, res, tmp );
		}

		return res;
	}


	private Expression parseExpression2()
	{
		Expression res = parseExpression3();
		while( currentTerminal.isMulOperator() ) {
			Operator op = parseOperator();
			Expression tmp = parseExpression3();

			res = new BinaryExpression( op, res, tmp );
		}

		return res;
	}

	private Expression parseExpression3()
	{
		Expression res = parsePrimary();
		while( currentTerminal.isCompOperator() ) {
			Operator op = parseOperator();
			Expression tmp = parsePrimary();

			res = new BinaryExpression( op, res, tmp );
		}

		return res;
	}

	private Expression parsePrimary()
	{
		switch(currentTerminal.kind)
		{
			case IDENTIFIER:
				Identifier id = parseIdentifier();
				if(currentTerminal.kind == LEFTARROW)
				{
					accept(LEFTARROW);
					TypeValue typeValueClass = parseType();
					accept(RIGHTARROW);
					accept(LEFTSQUARE);
					Expression exp = parseExpression();
					accept(RIGHTSQUARE);
					return new ArrayExpression(id, typeValueClass, exp);
				}
				if(currentTerminal.kind == LEFTPARAN)
				{
					accept(LEFTPARAN);
					Expression exp = parseExpression();
					accept(RIGHTPARAN);
					return new ArrayExpression(id, exp);
				}
				if(currentTerminal.kind == LEFTSQUARE)
				{
					accept(LEFTSQUARE);
					ExpList exp = parseExpList();
					accept(RIGHTSQUARE);
					return new CallExpression(id, exp);
				}
				return new VarExpression(id);

			case INTEGERLITERAL:
				IntegerLiteral lit = parseIntegerLiteral();
				return new IntLitExpression(lit);

			case OPERATOR:
				Operator op = parseOperator();
				Expression exp = parsePrimary();
				return new UnaryExpression(op, exp);

			case LEFTPARAN:
				accept(LEFTPARAN);
				Expression res = parseExpression();
				accept(RIGHTPARAN);
				return res;

			case VALUE:
				Expression val = parseValue();
				return val;

			default:
				System.out.println("Error in primary");
				return null;
		}
	}

	private Expression parseValue()
	{
		if(currentTerminal.kind == VALUE)
		{
			Value value = new Value(Boolean.parseBoolean(currentTerminal.spelling));
			currentTerminal = scan.scan();

			return value;
		}
		else
		{
			System.out.println("Integer literal expected");

			return new Value();
		}
	}

	private Operator parseOperator()
	{
		if(currentTerminal.kind == OPERATOR)
		{
			Operator res = new Operator(currentTerminal.spelling);
			currentTerminal = scan.scan();

			return res;
		}
		else
		{
			System.out.println("Operator expected");

			return new Operator("???");
		}
	}

	private void accept(TokenKind expected)
	{
		if(currentTerminal.kind == expected)
		{
			System.out.println(expected + "   " + currentTerminal.spelling);
			currentTerminal = scan.scan();
		}
		else
			System.out.println("Expected token of kind " + expected);
	}
}
