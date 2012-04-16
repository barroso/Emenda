import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;


public class EmendaAntFile {
	
	private static String repositorioDasVersoes = "C:\\repositorio\\fortesrhwar";
	private static String versaoCliente = "v1";
	private static String versaoAtualizada = "v3";
	private static String template = "TemplateAntCopyDel.xml";

	private static Collection<String> criadosOuModificados = new ArrayList<String>();
	private static Collection<String> deletados = new ArrayList<String>();
	private static StringBuilder includeCopy = new StringBuilder();
	private static StringBuilder includeDel = new StringBuilder();

	public static void main(String[] args) {
		try {
			System.out.println("Iniciando criação do build...");
			Process saida = Runtime.getRuntime().exec("git_diff.bat " + versaoCliente + " " + versaoAtualizada + " " + repositorioDasVersoes);
	        BufferedReader buffer = new BufferedReader(new InputStreamReader(saida.getInputStream()));
	        
	        getModificacoes(buffer);
	        
	        montaCopy();
	        montaDelete();
			
	        gerarFileAtualizador();
	        
	        System.out.println("Build criado!");
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	private static void getModificacoes(BufferedReader buf) throws IOException 
	{
		String comando = "";
		String filePath = "";
		String linha = "";
		boolean capturaComandos = false;
		
		while((linha = buf.readLine()) != null) 
		{
			if(linha.contains("git diff"))
			{
				capturaComandos = true;
				continue;
			}
			
			if(capturaComandos)
			{
				String[] comandoAndFile = linha.split("	");
				comando = comandoAndFile[0];
				filePath = comandoAndFile[1];
				
				if(comando.equals("M") || comando.equals("A"))
					criadosOuModificados.add(filePath);
				
				if(comando.equals("D"))
					deletados.add(filePath);
			}
		}
	}

	private static void montaCopy() 
	{
		String filePathDir = "";
		for (String copiado : criadosOuModificados)
		{
			if(copiado.contains("/"))
				filePathDir = copiado.substring(0, copiado.lastIndexOf("/"));
			else
				filePathDir = "";

			includeCopy.append("\t\t<copy todir='${destino}/"+ filePathDir +"'>");
			includeCopy.append("<fileset file='${origem}/"+ copiado +"'/>");
			includeCopy.append("</copy>\n");
		}
	}
	
	private static void montaDelete() 
	{
		for (String del : deletados)
			includeDel.append("\t\t<delete file='${destino}//"+ del +"'/>\n");
	}
	
	private static void gerarFileAtualizador() throws IOException
	{
		StringBuilder conteudo = lerTemplateAnt(template);

		FileWriter fileWriter = new FileWriter("atualizador_"+ versaoCliente +"_"+ versaoAtualizada +".xml");
		fileWriter.write(conteudo.toString().replace("//COPY_INCLUDE", includeCopy).replace("//COPY_DEL", includeDel));

		fileWriter.flush();
		fileWriter.close();
	}

	private static StringBuilder lerTemplateAnt(String pathXML) throws IOException
	{
		StringBuilder codigoTemplate = new StringBuilder();
		BufferedReader in = new BufferedReader(new FileReader(pathXML));

		while (in.ready())
			codigoTemplate.append(in.readLine() + "\n");

		in.close();
		return codigoTemplate;
	}
}