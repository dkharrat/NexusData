package com.nexusdata.modelgen;

import com.google.gson.*;
import com.nexusdata.modelgen.metamodel.*;
import freemarker.template.*;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

public class ModelGenerator {

    static Logger LOG = LoggerFactory.getLogger(ModelGenerator.class);

    void generateModels(String modelPath, File outputDir) throws IOException {
        LOG.info("Setting up model generator");

        try {
            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_SLF4J);
        } catch (Exception e) {
            LOG.warn("Could not set logging for freemarker");
        }
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        cfg.setDirectoryForTemplateLoading(getTemplatesPath());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setWhitespaceStripping(true);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));

        LOG.info("Parsing model file '{}'", modelPath);
        Model model = parseFile(modelPath);

        LOG.info("Generating class files for '{}' model (version {})", model.getName(), model.getVersion());
        outputDir.mkdir();

        Template generatedModelTemplate = cfg.getTemplate("generated_model.ftl");
        Template userModelTemplate = cfg.getTemplate("user_model.ftl");

        for (Entity entity : model.getEntities()) {
            SimpleHash root = new SimpleHash();
            root.put("entity", entity);
            root.put("packageName", model.getPackageName());

            String userModelFileName = entity.getName() + ".java";
            String genModelFileName = "_" + userModelFileName;

            try {
                File userModelFile = new File(outputDir, userModelFileName);
                if (!userModelFile.exists()) {
                    LOG.info("Generating class {}", userModelFileName);
                    Writer userModelOut = new FileWriter(userModelFile);
                    userModelTemplate.process(root, userModelOut);
                }

                LOG.info("Generating class {}", genModelFileName);
                Writer genModelOut = new FileWriter(new File(outputDir, genModelFileName));
                generatedModelTemplate.process(root, genModelOut);
            } catch (TemplateException ex) {
                throw new RuntimeException("Could not generate class files", ex);
            }
        }
    }

    File getTemplatesPath() {
        URL url = this.getClass().getClassLoader().getResource("templates");
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to open templates directory");
        }
    }

    Model parseFile(String filePath) throws IOException {

        File file = new File(filePath);
        byte[] bytes = Files.readAllBytes(file.toPath());
        String json = new String(bytes, "UTF-8");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Entity.class, new EntityDeserializer())
                .create();

        ModelWrapper rootModel = gson.fromJson(json, ModelWrapper.class);

        // later on, when we have multiple version formats, we'll need to parse
        // model based on metaVersion
        return gson.fromJson(rootModel.model, Model.class);
    }

    static class EntityDeserializer implements JsonDeserializer<Entity> {

        Gson gson = new Gson();

        @Override
        public Entity deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {

            final Entity entity = gson.fromJson(json, typeOfT);
            for (Attribute attr : entity.getAttributes()) {
                attr.setEntity(entity);
            }
            return entity;
        }
    }
}
