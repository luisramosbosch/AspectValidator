
# Intended Use
AspectValidator can be used to check th fulfilment of [Bosch Modeling Borad Guidelines](https://inside-docupedia.bosch.com/confluence/display/semstack/WS%3A+Aspect+Model+Modelling+Guideline#WS:AspectModelModellingGuideline-Descriptions). More specifically:

- preferredName, in English, it will always change any capital letter to lower case.
- Any special character (!"§$%&/()=?*+~#') in prefName will be remove.
- Description text must begin with capital and must end in a "."
- preferredName and description must be present, otherwise a report is generated.
- It checks that every property, with a primitive datatype, has en example value.

# Run application.

1. Clone https://github.com/luisramosbosch/AspectValidator.git
2. Navigate to AspectValidator\target
