# Math Grapher ProGuard Rules
# Keep math engine classes for potential reflection-based testing
-keep class com.prasad.mathgrapher.math.** { *; }

# Compose rules are handled automatically by the Compose compiler plugin
