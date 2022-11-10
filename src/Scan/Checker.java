package Scan;

import java.util.*;
import AST.*;

public class Checker
    implements Visitor
{
  private IdentificationTable idTable = new IdentificationTable();


  public void check( Program p )
  {
    p.visit( this, null );
  }


  public Object visitProgram( Program p, Object arg )
  {
    idTable.openScope();

    p.block.visit( this, null );

    idTable.closeScope();

    return null;
  }


  public Object visitBlock( Block b, Object arg )
  {
    b.decs.visit( this, null );
    b.stats.visit( this, null );

    return null;
  }


  public Object visitDeclarations( Declarations d, Object arg )
  {
    for( Declaration decl: d.dec )
      decl.visit( this, null );

    return null;
  }


  public Object visitVariableDeclaration( VariableDeclaration v, Object arg )
  {
    String id = (String) v.id.visit( this, null );

    idTable.enter( id, v );

    return null;
  }


  public Object visitFunctionDeclaration( FunctionDeclaration f, Object arg )
  {
    String id = (String) f.name.visit( this, null );

    idTable.enter( id, f );
    idTable.openScope();

    f.params.visit( this, null );
    f.block.visit( this, null );
    if(!f.typeValueClass.type.equals(TypeValue.TypeName.NULL))
      f.retExp.visit( this, null );

    idTable.closeScope();

    return null;
  }


  public Object visitStatements( Statements s, Object arg )
  {
    for( Statement stat: s.stat )
      stat.visit( this, null );

    return null;
  }


  public Object visitExpressionStatement( ExpressionStatement e, Object arg )
  {
    e.exp.visit( this, null );

    return null;
  }


  public Object visitIfStatement( IfStatement i, Object arg )
  {
    i.exp.visit( this, null );
    i.thenPart.visit( this, null );
    if(i.elsePart != null)
      i.elsePart.visit( this, null );

    return null;
  }


  public Object visitWhileStatement( WhileStatement w, Object arg )
  {
    w.exp.visit( this, null );
    w.stats.visit( this, null );

    return null;
  }

  @Override public Object visitPrintStatement(PrintStatement s, Object arg)
  {
    s.exp.visit(this,null);
    return null;
  }

  @Override public Object visitValue(Value s, Object arg)
  {
    return s.value;
  }

  @Override public Object visitArrayExpression(ArrayExpression a,
      Object arg)
  {
//    Identifier id = (Identifier) a.id.visit(this,null);
//    Expression exp = (Expression) a.exp.visit(this,null);
//    TypeValue typeValueClass = (TypeValue) a.typeValueClass.visit(this,null);

    return null;
  }

  @Override public Object visitInputStatement(InputStatement s, Object arg)
  {
    s.idf.visit(this,null);
    return null;
  }

  public Object visitBinaryExpression( BinaryExpression b, Object arg )
  {
    TypeValue t1 = (TypeValue) b.operand1.visit(this, null);
    TypeValue t2 = (TypeValue) b.operand2.visit(this, null);
    String operator = (String) b.operator.visit( this, null );

    if( operator.equals( "->" ) && t1.rvalueOnly )
      System.out.println( "Left-hand side of -> must be a variable" );

    return new TypeValue(true );
  }


  public Object visitVarExpression( VarExpression v, Object arg )
  {
    String id = (String) v.name.visit( this, null );

    Declaration d = idTable.retrieve( id );
    if( d == null )
      System.out.println( id + " is not declared" );
    else if( !( d instanceof VariableDeclaration ) )
      System.out.println( id + " is not a variable" );
    else
      v.decl = (VariableDeclaration) d;

    return new TypeValue(false );
  }


  public Object visitCallExpression( CallExpression c, Object arg )
  {
    String id = (String) c.name.visit( this, null );
    Vector<TypeValue> t = (Vector<TypeValue>)( c.args.visit(this, null) );

    Declaration d = idTable.retrieve( id );
    if( d == null )
      System.out.println( id + " is not declared" );
    else if( !( d instanceof FunctionDeclaration ) )
      System.out.println( id + " is not a function" );
    else {
      FunctionDeclaration f = (FunctionDeclaration) d;
      c.decl = f;

      if( f.params.dec.size() != t.size() )
        System.out.println( "Incorrect number of arguments in call to " + id );
    }

    return new TypeValue(true );
  }


  public Object visitUnaryExpression( UnaryExpression u, Object arg )
  {
    u.operand.visit( this, null );
    String operator = (String) u.operator.visit( this, null );

    if( !operator.equals( "+" ) && !operator.equals( "-" ) )
      System.out.println( "Only + or - is allowed as unary operator" );

    return new TypeValue(true );
  }


  public Object visitIntLitExpression( IntLitExpression i, Object arg )
  {
    i.literal.visit( this, true );

    return new TypeValue(true );
  }

  @Override public Object visitFuncExpression(FuncExpression f, Object arg)
  {
    return null;
  }

  public Object visitExpList( ExpList e, Object arg )
  {
    Vector<TypeValue> typeValues = new Vector<TypeValue>();

    for( Expression exp: e.exp )
      typeValues.add((TypeValue) exp.visit(this, null));

    return typeValues;
  }


  public Object visitIdentifier( Identifier i, Object arg )
  {
    return i.spelling;
  }


  public Object visitIntegerLiteral( IntegerLiteral i, Object arg )
  {
    return null;
  }


  public Object visitOperator( Operator o, Object arg )
  {
    return o.spelling;
  }
}