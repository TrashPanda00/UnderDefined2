/*
 * 27.09.2016 Minor edit
 * 11.10.2010 dump() removed
 * 21.10.2009 New folder structure
 * 29.09.2006 Original version
 */
 
package AST;


public class VariableDeclaration
	extends Declaration
{
	public TypeClass typeClass1;
	public TypeClass typeClass2;
	public IntegerLiteral lit;
	public Identifier id;
	public Statements stat;
	public Statement statSingle;

	public VariableDeclaration(TypeClass typeClass1, Identifier id)
	{
		this.typeClass1 = typeClass1;
		this.typeClass2 = typeClass2;
		this.id = id;
	}

	public VariableDeclaration(TypeClass typeClass1, Identifier id, Statement stat)
	{
		this.typeClass1 = typeClass1;
		this.id = id;
		this.statSingle = stat;
	}

	public VariableDeclaration(TypeClass typeClass1, TypeClass typeClass2, IntegerLiteral lit, Identifier id, Statements stat)
	{
		this.typeClass1 = typeClass1;
		this.typeClass2 = typeClass2;
		this.lit = lit;
		this.id = id;
		this.stat = stat;
	}
	public Object visit( Visitor v, Object arg )
	{
		return v.visitVariableDeclaration( this, arg );
	}


}