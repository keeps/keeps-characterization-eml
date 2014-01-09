package pt.keep.validator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Entity;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.keep.validator.result.Result;
import pt.keep.validator.result.ValidationInfo;


public class EmlCharacterizationTool {
  private static String version = "1.0";

  private StringBuffer txtBody;
  private StringBuffer htmlBody;
  private ArrayList<BodyPart> attachments;  

  
  
  
  public EmlCharacterizationTool() {
    txtBody = new StringBuffer();
    htmlBody = new StringBuffer();
    attachments = new ArrayList<BodyPart>();
  }

  public String getVersion() {
    return version;
  }

  public String run(File f) {
    try {

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Result res = process(f);
      JAXBContext jaxbContext = JAXBContext.newInstance(Result.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      jaxbMarshaller.marshal(res, bos);
      return bos.toString("UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public Result process(File f) {
    Result res = new Result();
    Map<String, String> features = new HashMap<String, String>();
    FileInputStream fis = null;

    ValidationInfo val = new ValidationInfo();
    val.setValid(true);
    MimeEntityConfig config = new MimeEntityConfig();
    config.setMaxLineLen(-1);
    try {
      fis = new FileInputStream(f);
      Message mimeMsg = new Message(fis,config);

      if(mimeMsg.getTo()!=null){
        features.put("to", mimeMsg.getTo().toString());
      }
      if(mimeMsg.getFrom()!=null){
        features.put("from", mimeMsg.getFrom().toString());
      }
      if(mimeMsg.getSubject()!=null){
        features.put("subject", mimeMsg.getSubject() );
      }
      

      Field priorityFld = mimeMsg.getHeader().getField("X-Priority");
      if (priorityFld != null) {
        features.put("priority", priorityFld.getBody());
      }

      if (mimeMsg.isMultipart()) {
        Multipart multipart = (Multipart) mimeMsg.getBody();
        parseBodyParts(multipart);
      } else {
        String text = getTxtPart(mimeMsg);
        txtBody.append(text);
      }

      features.put("textBody", txtBody.toString());
      features.put("htmlBody", htmlBody.toString());
     


    } catch (Exception ex) {
      ex.printStackTrace();
      val.setValid(false);
      val.setValidationError(ex.getMessage());
      features.clear();
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
    res.setValidationInfo(val);
    res.setFeatures(features);
    return res;
  }

  private void parseBodyParts(Multipart multipart) throws IOException {
    for (BodyPart part : multipart.getBodyParts()) {
      if (part.isMimeType("text/plain")) {
        String txt = getTxtPart(part);
        txtBody.append(txt);
      } else if (part.isMimeType("text/html")) {
        String html = getTxtPart(part);
        htmlBody.append(html);
      } else if (part.getDispositionType() != null && !part.getDispositionType().equals("")) {
        attachments.add(part);
      }

      if (part.isMultipart()) {
        parseBodyParts((Multipart) part.getBody());
      }
    }
  }

  private String getTxtPart(Entity part) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    if(part.getBody() instanceof Message){
      Message m = (Message) part.getBody();
    }else if(part.getBody() instanceof Multipart){
      Multipart m = (Multipart) part.getBody();
    }else if(part.getBody() instanceof BinaryBody){
      BinaryBody b = (BinaryBody) part.getBody();
    }else if(part.getBody() instanceof TextBody){
      TextBody t = (TextBody)part.getBody();
      t.writeTo(baos);
      return new String(baos.toByteArray());
    }
    return new String();
  }




  private void printHelp(Options opts) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar [jarFile]", opts);
  }

  private void printVersion() {
    System.out.println(version);
  }


  public static void main(String[] args) {
    Logger.getRootLogger().setLevel(Level.OFF);
    try {
      EmlCharacterizationTool rct = new EmlCharacterizationTool();
      Options options = new Options();
      options.addOption("f", true, "file to analyze");
      options.addOption("v", false, "print this tool version");
      options.addOption("h", false, "print this message");

      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        rct.printHelp(options);
        System.exit(0);
      }

      if (cmd.hasOption("v")) {
        rct.printVersion();
        System.exit(0);
      }

      if (!cmd.hasOption("f")) {
        rct.printHelp(options);
        System.exit(0);
      }

      File f = new File(cmd.getOptionValue("f"));
      if (!f.exists()) {
        System.out.println("File doesn't exist");
        System.exit(0);
      }
      String toolOutput = rct.run(f);
      if (toolOutput != null) {
        System.out.println(toolOutput);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
