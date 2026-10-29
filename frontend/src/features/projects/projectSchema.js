import { z } from 'zod'

export const projectSchema = z
  .object({
    title: z
      .string()
      .trim()
      .min(3, 'Le titre doit comporter au moins 3 caractères')
      .max(200, 'Le titre ne peut pas dépasser 200 caractères'),
    description: z
      .string()
      .max(2000, 'La description ne peut pas dépasser 2000 caractères')
      .optional()
      .or(z.literal('')),
    status: z.enum(
      ['PROPOSED', 'PENDING', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED'],
      { errorMap: () => ({ message: 'Veuillez sélectionner un statut valide' }) }
    ),
    startDate: z.string().optional().or(z.literal('')),
    endDate: z.string().optional().or(z.literal('')),
  })
  .refine(
    (data) => {
      if (data.startDate && data.endDate) {
        return new Date(data.endDate) >= new Date(data.startDate)
      }
      return true
    },
    {
      message: 'La date de fin ne peut pas être antérieure à la date de début',
      path: ['endDate'],
    }
  )

export default projectSchema

