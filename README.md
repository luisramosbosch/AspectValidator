
# Intended Use
AspectValidator can be used to check th fulfilment of [Bosch Modeling Borad Guidelines](https://inside-docupedia.bosch.com/confluence/display/semstack/WS%3A+Aspect+Model+Modelling+Guideline#WS:AspectModelModellingGuideline-Descriptions). More specifically:

- preferredName, in English, it will always change any capital letter to lower case.
- Any special character (!"§$%&/()=?*+~#') in prefName will be remove.
- Description text must begin with capital and must end in a "."
- preferredName and description must be present, otherwise a report is generated.
- It checks that every property, with a primitive datatype, has en example value.

# Run application.

1. Check minimum installed java version in your system is 17 or higher:   `java -version`
2. Clone this repository: `git clone https://github.com/luisramosbosch/AspectValidator.git `
3. Navigate to AspectValidator\target and open the terminal o there.
4. Execute in your terminal: `java -jar AspectValidator-version-SNAPSHOT-uber.jar`
5. As the folllwing window prompts, please select the intended aspect file to analyse:
![image](https://github.com/user-attachments/assets/4572d36c-4e04-4ab6-bb67-84ad9c82b149)

6. The tool will ask you if you want to make a copy of the file (recommended), and after accepting, the file will be inspected.
7. If you do not want to analize further files, then it woill terminate.
8. Two new files will be generated:

   
![image](https://github.com/user-attachments/assets/981ba286-97c8-461d-82c5-f4a9e5fa150c)

A file called file_copy.ttl, which is the original, and initial file. And a file called file_logs.txt which contains detailed logs of the findings and changes done by the tool. 







