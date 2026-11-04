SIRI MOD - Minecraft 1.20.1 / Forge 47.x

COMO GERAR O .JAR
1) Instale o JDK 17.
2) Opcao A (recomendada): baixe o Forge MDK 1.20.1 (47.3.0) em files.minecraftforge.net,
   extraia, APAGUE a pasta 'src' do MDK e coloque a pasta 'src' deste projeto no lugar.
   Em gradle.properties do MDK ponha: mod_id=siri  mod_group_id=com.example.siri  mod_version=1.0.0
   Rode:  ./gradlew build     (Windows: gradlew.bat build)
   Opcao B: com Gradle 8.x instalado, rode 'gradle build' aqui.
3) O jar sai em build/libs/siri-1.0.0.jar -> coloque na pasta mods do Minecraft 1.20.1 com Forge.

COMO USAR
- Spawn egg na aba Spawn Eggs. Aparece naturalmente em praias e desertos.
- Clique com balde vazio ou balde de agua no siri para pega-lo (ele vira 'Balde com Siri').
- O siri se enterra na areia quando voce chega perto (agachado ele deixa chegar mais perto),
  quando leva dano ou aleatoriamente, e sai com um pulinho depois de um tempo.
  Enterrado ele fica invisivel, intocavel e invulneravel.
