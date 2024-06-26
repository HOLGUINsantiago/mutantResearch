#!/bin/bash 

fichiersTempPath="src/main/resources/temp"

if [ -z "$fichiersTempPath" ]; then
    exit 1
fi
if [ "$(tail -n 1 "$fichiersTempPath/output_file.fasta" | cut -c1)" = ">" ]; then
    sed -i '$d' "$fichiersTempPath/output_file.fasta"
fi
echo "La carpeta de archivos temporales es: $fichiersTempPath"

src/main/resources/static/alignement/ssearch36 -m 8 -E 4 "$fichiersTempPath/mutations.fasta" "$fichiersTempPath/output_file.fasta" > "$fichiersTempPath/results.tsv"
