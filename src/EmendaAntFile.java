import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;


public class EmendaAntFile {
	
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
			

			executeDiff();
			
	        getModificacoes();
	        
	        montaCopy();
	        montaDelete();
			
	        gerarFileAtualizador();
	        System.out.println("Build criado!");
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	private static void executeDiff()
	{
		File buildFile = new File("git.xml");
		Project project = new Project();
		project.setUserProperty("ant.file", buildFile.getAbsolutePath());
		project.fireBuildStarted();
		project.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		project.addReference("ant.projectHelper", helper);
		helper.parse(project, buildFile);
		project.executeTarget(project.getDefaultTarget());
	}

	private static void getModificacoes() throws IOException 
	{
		String comando = "";
		String filePath = "";
		String linha;
		BufferedReader fileDiff = new BufferedReader(new FileReader("diff_"+ versaoCliente +"_"+ versaoAtualizada +".txt"));
		
		while((linha = fileDiff.readLine()) != null)
		{
			if(!linha.equals(""))
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
		
		fileDiff.close();
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