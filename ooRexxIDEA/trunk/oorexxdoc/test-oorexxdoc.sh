#!/bin/bash

# Define the absolute path to the zip file and the unpacked folder
VERSION="2.4.1-GA"
ZIP_FILE="/Users/alex/IdeaProjects/ooRexxIDEAPlugin/oorexxdoc/build/distributions/ooRexxDoc-$VERSION.zip"
UNPACKED_FOLDER="/Users/alex/IdeaProjects/ooRexxIDEAPlugin/oorexxdoc/build/distributions/ooRexxDoc-$VERSION"
CP="/Users/alex/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea/ideaIC/2024.1/181fa36f74690e64a81a8e06ceda9480d2a6c626/ideaIC-2024.1/lib/*"
# Check if the zip file exists
if [ -f "$ZIP_FILE" ]; then
  echo "Zip file exists. Unpacking..."

  # Remove the existing unpacked folder if it exists
  if [ -d "$UNPACKED_FOLDER" ]; then
    echo "Unpacked folder already exists. Removing it..."
    rm -rf "$UNPACKED_FOLDER"
  fi

  # Unpack the zip file
  unzip "$ZIP_FILE" -d "$UNPACKED_FOLDER"

  # Check if the unzip command was successful
  if [ $? -eq 0 ]; then
    echo "Unpacking successful. Running subsequent commands..."

    # Call some commands afterwards
    # Replace the following echo commands with your actual commands
    cd "$UNPACKED_FOLDER/example"
    java -cp "../oorexxdoc-$VERSION.jar:$CP" org.oorexx.doc.Application ./ooRexxDoc-markdown.xml
    java -cp "../oorexxdoc-$VERSION.jar:$CP" org.oorexx.doc.Application ./ooRexxDoc.xml
  else
    echo "Failed to unpack the zip file."
    exit 1
  fi
else
  echo "Zip file does not exist."
fi
