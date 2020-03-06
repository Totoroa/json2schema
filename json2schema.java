import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

class Tree{
	public String key;
	public String valueType;
	public int num;
	public Tree child;
	public Tree brother;
	public String schemaStr="";
	
	public Tree(){}
	/*
	 * k key
	 * vt valueType
	 */	
	public Tree(String k,String vt){
		key=k;
		valueType=vt;
		num=1;
		child=null;
		brother=null;
	}
	
	public boolean hasChild(Tree T){	//Return true if T has child
		if(T.child==null){
			return false;
		}
		else{
			return true;
		}
	}
	
	public boolean hasBrother(Tree T){	//Return true if T has brother
		if(T.brother==null){
			return false;
		}
		else{
			return true;
		}
	}
	
	public void addChild(Tree kid){	//add one child
		if(!hasChild(this)){
			this.child=kid;
		}
		else{
			Tree temp=this.child;
			for(;temp.brother!=null;temp=temp.brother);
			temp.brother=kid;
		}
	}
	
	public void addChild(Tree root,Tree kid){	//add one child to root
		if(!hasChild(root)){
			root.child=kid;
		}
		else{
			Tree temp=root.child;
			for(;temp.brother!=null;temp=temp.brother);
			temp.brother=kid;
		}
	}
	public void printTree(FileWriter fw) throws IOException{			
		if(this==null){return;}
		if(this.key=="schema"){
			Tree ttt1;
			for(ttt1=this.child;ttt1!=null;ttt1=ttt1.brother){
				ttt1.printTree(fw);
			}
		}
		else{
			if(this.child==null){
				//System.out.println(this.valueType+" "+this.key+";");
				fw.write(this.valueType+" "+this.key+";");
			}
			else{
				//System.out.println(this.valueType+" "+this.key);
				fw.write(this.valueType+" "+this.key);
			}
			Tree ttt;
			int i=0;
			for(ttt=this.child;ttt!=null;ttt=ttt.brother){
				if(i==0){/*System.out.print("{");*/fw.write("{");}
				ttt.printTree(fw);
				i++;
			}
			if(i>0){
				//System.out.println("}");
				fw.write("}");
			}
		}
	}
}

public class json2schema {
	public static ArrayList<String> jsonString=new ArrayList<String>();	//element is a json object
	static Tree schema_root=new Tree("schema","schema");
	
	public static boolean isInteger(String aa){
		try {
			Integer.parseInt(aa);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public static boolean isDouble(String bb){
		try {
			Double.parseDouble(bb);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static String judgeType(String valTp){
		if(valTp.charAt(0)=='"')	{return "optional binary";}
		//else if(valTp.charAt(0)=='{')	{return "optional group";}
		else if(isInteger(valTp))	{return "optional int64";}
		else if(isDouble(valTp))	{return "optional double";}
		else {return "OTHER TYPE!";}
	}
	
	public static ArrayList<String> getTreeKey(Tree tr){
		ArrayList<String> keys=new ArrayList<String>();
		
		if(tr.child==null){return keys;}
		Tree tp1;
		for(tp1=tr.child;tp1.brother!=null;tp1=tp1.brother){
			keys.add(tp1.key);
		}
		keys.add(tp1.key);
		return keys;
	}
	
	public static String getFirstEle(String Arr){	//得到[]中的第一个元素
		String elem="";
		if(Arr.charAt(1)=='{'){
			elem+=Arr.charAt(1);
			int n=1;
			for(int i=2;i<Arr.length();i++){
				elem+=Arr.charAt(i);
				if(Arr.charAt(i)=='{'){n++;}
				if(Arr.charAt(i)=='}'){n--;}
				if(n==0){break;}
			}
		}
		else{
			for(int i=1;i<Arr.length();i++){
				if(Arr.charAt(i)==','){break;}
				elem+=Arr.charAt(i);
			}
		}
		return elem;
	}
	
	public static void buildTree(Tree root,String jsonStr){	//提取出当前的（key，valueType），添加到root下
		Stack<Character> bracket = new Stack<Character>();
		Stack<Character> quotation = new Stack<Character>();
		
		int i=0;
		int flag=0;		//0:key		1:value
		String key="";
		String value="";
		ArrayList<String> key_list=new ArrayList<String>();
		ArrayList<String> value_list=new ArrayList<String>();
		ArrayList<String> key_exist=getTreeKey(root);
		
		while(i<jsonStr.length()){
			if(jsonStr.charAt(i)=='{')	{bracket.push(jsonStr.charAt(i));i++;}
			else if(jsonStr.charAt(i)=='"' && flag==0){
				i++;
				for(;jsonStr.charAt(i)!='"';i++)	{key+=jsonStr.charAt(i);}
				key+=" ";
				key_list.add(key);
				key="";
				i++;
				flag=1;
			}
			else if(jsonStr.charAt(i)==':'){
				i++;
				if(jsonStr.charAt(i)=='['){
					for(;jsonStr.charAt(i)!=']';i++)	{value+=jsonStr.charAt(i);}
					value+="] ";
					value_list.add(value); 
					value="";
					i+=2;
					flag=0;
				}
				else if(jsonStr.charAt(i)=='{'){
					value+='{';
					bracket.push('{');
					i++;
					for(;!(bracket.size()==1);i++){
						if(jsonStr.charAt(i)=='{'){bracket.push('{');value+='{';}
						else if(jsonStr.charAt(i)=='}'){bracket.pop();value+='}';}
						else{
							value+=jsonStr.charAt(i);
						}
					}
					value_list.add(value);
					value="";
					i+=1;
					flag=0;
				}
				else{
					for(;jsonStr.charAt(i)!=',';i++)	{if(jsonStr.charAt(i)=='}'){break;} value+=jsonStr.charAt(i);}
					i++;
					value+=" ";
					value_list.add(value);
					value="";
					flag=0;
				}
			}
			else{
				i++;
			}
		}
		
		//System.out.println("\nkey_list= ");
		//for(int ii = 0;ii < key_list.size(); ii++){
        //    System.out.println(key_list.get(ii)+" ");
        //}
		//System.out.println("\nvalue_list= ");
		//for(int jj = 0;jj < value_list.size(); jj++){
        //    System.out.println(value_list.get(jj)+" ");
        //}

		//***************************************************
		//这里已经得到了keys和values，需要判断values的类型。
		//如果value第一个字符是'['，则调用getFirstEle函数得到其第一个元素fir，判断元素类型。
		//如果fir类型为"optional group"，则对其对应的key的树执行buildTree。
		
		for(int j=0;j<key_list.size();j++){
			if(key_exist.contains(key_list.get(j))){continue;}
			if(value_list.get(j).charAt(0)=='['){
				String a=getFirstEle(value_list.get(j));
				if(a.charAt(0)=='{'){
					Tree n1=new Tree(key_list.get(j),"repeated group");
					root.addChild(n1);
					buildTree(n1,a);
				}
				else{
					Tree n2=new Tree(key_list.get(j),judgeType(value_list.get(j)));
					root.addChild(n2);
				}
			}
			else if(value_list.get(j).charAt(0)=='{'){
				Tree n3=new Tree(key_list.get(j),"optional group");
				root.addChild(n3);
				buildTree(n3,value_list.get(j));
			}
			else{
				Tree n4=new Tree(key_list.get(j),judgeType(value_list.get(j).trim()));
				root.addChild(n4);
			}
		}
		
		//***************************************************
	}
	
	public static String scheStr(String jsonFile) throws IOException{
		try {
			BufferedReader in = new BufferedReader(new FileReader(jsonFile));
			String str;
            while ((str = in.readLine()) != null) {
            	jsonString.add(str);
            	//System.out.println("111"+str);
            	buildTree(schema_root,str);
                //System.out.println(str);
            }
		} catch (FileNotFoundException|OutOfMemoryError e) {
			// TODO Auto-generated catch block
			System.out.println("FileNotFoundException|OutOfMemoryError");
		}
		finally{
		//create a temp file to save schema string.
		File f=new File("scheStr.txt");
		if(f.exists()){f.delete();}
		FileWriter fw=new FileWriter(f,true);
		//System.out.println("message Document{");
		fw.write("message Document{");
		schema_root.printTree(fw);
		fw.write("}");
		fw.close();
		
		BufferedReader br =new BufferedReader(new FileReader(f));
		String sss="";
		String tp="";
		while ((tp = br.readLine()) != null) {
			sss+=tp;
		}
		br.close();
		f.delete();
		return sss;}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//if(args.length<1){System.out.println("Please input file:");System.exit(0);}
		//String filename=args[0];
		//String filename="person.json";
		String filename="C:/Users/lenovo/Desktop/workspace/JustForTest/person.json";
		String aaa=scheStr(filename);
		System.out.print(aaa);
	}
}
