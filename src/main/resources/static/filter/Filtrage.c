#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINE_LENGTH 1024
#define MOTIF_LENGTH 5
#define STRICT_MOTIF_LENGTH 10

typedef struct
{
    char **motifs;
    int motifs_count;
    char **strict_motifs;
    int strict_motifs_count;
} Sequences;

int count_differences(const char *seq1, const char *seq2, int length, int seuil)
{
    int differences = 0;
    for (int i = 0; i < length; i++)
    {
        if (seq1[i] != seq2[i])
        {
            differences++;
            if (differences > seuil)
            {
                return differences;
            }
        }
    }
    return differences;
}

int has_motif(const char *read, Sequences *sequences, int marge)
{
    int read_length = strlen(read);
    for (int i = 0; i <= read_length - MOTIF_LENGTH; i++)
    {
        for (int j = 0; j < sequences->motifs_count; j++)
        {
            if (count_differences(read + i, sequences->motifs[j], MOTIF_LENGTH, marge) < marge)
            {
                return 1;
            }
        }
    }
    return 0;
}

int has_stricted(const char *read, Sequences *sequences, int marge)
{
    int read_length = strlen(read);
    for (int i = 0; i <= read_length - STRICT_MOTIF_LENGTH; i++)
    {
        for (int j = 0; j < sequences->strict_motifs_count; j++)
        {
            if (count_differences(read + i, sequences->strict_motifs[j], STRICT_MOTIF_LENGTH, marge) < marge)
            {
                return 1;
            }
        }
    }
    return 0;
}

void filter(const char *input_path, const char *output_path, Sequences *sequences, int margeMotif, int margeStricted)
{
    FILE *input_file = fopen(input_path, "r");
    FILE *output_file = fopen(output_path, "w");

    if (!input_file || !output_file)
    {
        perror("Error opening file");
        exit(EXIT_FAILURE);
    }

    char line[MAX_LINE_LENGTH];
    int compt = 0;
    char id[MAX_LINE_LENGTH] = "";
    while (fgets(line, sizeof(line), input_file))
    {
        // Remove newline character if present
        line[strcspn(line, "\n")] = '\0';

        if (line[0] == '>' || line[0] == '@')
        {
            strcpy(id, line);
        }
        else
        {
            if (has_stricted(line, sequences, margeStricted) && has_motif(line, sequences, margeMotif))
            {
                fprintf(output_file, "%s\n%s\n", id, line);
            }
            compt++;
        }
    }

    fclose(input_file);
    fclose(output_file);
}

char **read_motifs(const char *file_path, int *count)
{
    FILE *file = fopen(file_path, "r");
    if (!file)
    {
        perror("Error opening motif file");
        exit(EXIT_FAILURE);
    }

    char **motifs = NULL;
    char line[MAX_LINE_LENGTH];
    *count = 0;

    while (fgets(line, sizeof(line), file))
    {
        line[strcspn(line, "\n")] = '\0';
        motifs = realloc(motifs, (*count + 1) * sizeof(char *));
        motifs[*count] = strdup(line);
        (*count)++;
    }

    fclose(file);
    return motifs;
}

void free_motifs(char **motifs, int count)
{
    for (int i = 0; i < count; i++)
    {
        free(motifs[i]);
    }
    free(motifs);
}

int main(int argc, char *argv[])
{
    // Read motifs and strict motifs from files
    const char *motifs_file = "temp/motifs.fasta";
    const char *strict_motifs_file = "temp/stricted.fasta";

    Sequences sequences;
    sequences.motifs = read_motifs(motifs_file, &sequences.motifs_count);
    sequences.strict_motifs = read_motifs(strict_motifs_file, &sequences.strict_motifs_count);

    // Filter input file and write to output file
    const char *input_file = argv[1];
    const char *output_file = argv[2];
    filter(input_file, output_file, &sequences, atoi(argv[3]), atoi(argv[4]));

    return 0;
}
