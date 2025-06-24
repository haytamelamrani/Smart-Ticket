"use client"

import type React from "react"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Separator } from "@/components/ui/separator"
import {
  Upload,
  AlertCircle,
  CheckCircle2,
  Loader2,
  Tag,
  Clock,
  FileText,
  X,
  Mail,
  User,
  MessageSquare,
} from "lucide-react"
import { toast } from "sonner"
import { Navbar } from "@/components/navbar"

interface TicketFormData {
  title: string
  description: string
  category: string
  priority: string
  type: string
  userEmail: string
  attachments: File[]
}

const categories = [
  {
    value: "technical",
    label: "Problème technique",
    color: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300",
    icon: "🔧",
  },
  {
    value: "account",
    label: "Compte utilisateur",
    color: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300",
    icon: "👤",
  },
  {
    value: "billing",
    label: "Facturation",
    color: "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300",
    icon: "💳",
  },
  {
    value: "feature",
    label: "Demande de fonctionnalité",
    color: "bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300",
    icon: "✨",
  },
  {
    value: "bug",
    label: "Signalement de bug",
    color: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300",
    icon: "🐛",
  },
  {
    value: "other",
    label: "Autre",
    color: "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300",
    icon: "📋",
  },
]

const priorities = [
  { value: "low", label: "Faible", color: "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300", icon: "🟢" },
  {
    value: "medium",
    label: "Moyenne",
    color: "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300",
    icon: "🟡",
  },
  {
    value: "high",
    label: "Élevée",
    color: "bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300",
    icon: "🟠",
  },
  {
    value: "urgent",
    label: "Urgente",
    color: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300",
    icon: "🔴",
  },
]

const ticketTypes = [
  { value: "incident", label: "Incident", icon: "⚠️" },
  { value: "request", label: "Demande", icon: "📝" },
  { value: "complaint", label: "Réclamation", icon: "😤" },
  { value: "suggestion", label: "Suggestion", icon: "💡" },
]

export default function CreateTicketPage() {
  const [formData, setFormData] = useState<TicketFormData>({
    title: "",
    description: "",
    category: "",
    priority: "",
    type: "",
    userEmail: "",
    attachments: [],
  })

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitStatus, setSubmitStatus] = useState<"idle" | "success" | "error">("idle")
  const [ticketId, setTicketId] = useState<string>("")


  const handleInputChange = (field: keyof TicketFormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files || [])
    setFormData((prev) => ({ ...prev, attachments: [...prev.attachments, ...files] }))
  }

  const removeAttachment = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      attachments: prev.attachments.filter((_, i) => i !== index),
    }))
  }

  const validateForm = (): boolean => {
    if (!formData.title.trim()) {
      toast.error("Erreur de validation", {
        description: "Le titre est obligatoire",
      })
      return false
    }
    if (!formData.description.trim()) {
      toast.error("Erreur de validation", {
        description: "La description est obligatoire",
      })
      return false
    }
    if (!formData.category) {
      toast.error("Erreur de validation", {
        description: "Veuillez sélectionner une catégorie",
      })
      return false
    }
    if (!formData.priority) {
      toast.error("Erreur de validation", {
        description: "Veuillez sélectionner une priorité",
      })
      return false
    }
    if (!formData.type) {
      toast.error("Erreur de validation", {
        description: "Veuillez sélectionner un type de ticket",
      })
      return false
    }
    if (!formData.userEmail.trim() || !formData.userEmail.includes("@")) {
      toast.error("Erreur de validation", {
        description: "Veuillez saisir une adresse email valide",
      })
      return false
    }
  
    return true
  }
  

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) return

    setIsSubmitting(true)
    setSubmitStatus("idle")

    try {
      const formDataToSend = new FormData()
      formDataToSend.append("title", formData.title)
      formDataToSend.append("description", formData.description)
      formDataToSend.append("category", formData.category)
      formDataToSend.append("priority", formData.priority)
      formDataToSend.append("type", formData.type)
      formDataToSend.append("userEmail", formData.userEmail)

      formData.attachments.forEach((file, index) => {
        formDataToSend.append(`attachments[${index}]`, file)
      })

      await new Promise((resolve) => setTimeout(resolve, 2000))

      const mockTicketId = `TK-${Date.now().toString().slice(-6)}`
      setTicketId(mockTicketId)
      setSubmitStatus("success")

      toast.error("Ticket créé avec succès !",{
        
        description: `Votre ticket ${mockTicketId} a été créé et sera traité dans les plus brefs délais.`,
      })

      setFormData({
        title: "",
        description: "",
        category: "",
        priority: "",
        type: "",
        userEmail: "",
        attachments: [],
      })
    } catch (error) {
      setSubmitStatus("error")
      toast.error("Erreur lors de la création",{
        
        description: "Une erreur est survenue lors de la création du ticket. Veuillez réessayer.",
       
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const selectedCategory = categories.find((cat) => cat.value === formData.category)
  const selectedPriority = priorities.find((pri) => pri.value === formData.priority)
  const selectedType = ticketTypes.find((type) => type.value === formData.type)

  if (submitStatus === "success") {
    return (
      <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-green-50 to-teal-50 dark:from-emerald-950 dark:via-green-950 dark:to-teal-950">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-2xl mx-auto">
            <Card className="border-emerald-200 bg-emerald-50/50 dark:border-emerald-800 dark:bg-emerald-950/50 shadow-xl">
              <CardHeader className="text-center pb-8">
                <div className="mx-auto w-20 h-20 bg-gradient-to-br from-emerald-400 to-emerald-600 rounded-full flex items-center justify-center mb-6 shadow-lg">
                  <CheckCircle2 className="w-10 h-10 text-white" />
                </div>
                <CardTitle className="text-2xl text-emerald-800 dark:text-emerald-200">
                  Ticket créé avec succès !
                </CardTitle>
                <CardDescription className="text-emerald-600 dark:text-emerald-400 text-lg">
                  Votre demande a été enregistrée et sera traitée par notre équipe
                </CardDescription>
              </CardHeader>
              <CardContent className="text-center space-y-6">
                <div className="bg-white dark:bg-gray-800 p-6 rounded-xl border border-emerald-200 dark:border-emerald-800 shadow-sm">
                  <p className="text-sm text-muted-foreground mb-3">Numéro de ticket</p>
                  <p className="text-3xl font-bold text-emerald-600 dark:text-emerald-400 tracking-wider">{ticketId}</p>
                </div>

                <div className="bg-white dark:bg-gray-800 p-4 rounded-lg border border-emerald-200 dark:border-emerald-800">
                  <div className="flex items-center justify-center gap-2 text-muted-foreground">
                    <Mail className="w-4 h-4" />
                    <span className="text-sm">
                      Confirmation envoyée à <strong className="text-foreground">{formData.userEmail}</strong>
                    </span>
                  </div>
                </div>

                <div className="flex flex-col sm:flex-row gap-3 justify-center pt-6">
                  <Button
                    onClick={() => setSubmitStatus("idle")}
                    variant="outline"
                    className="border-emerald-300 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-700 dark:text-emerald-300 dark:hover:bg-emerald-950"
                  >
                    Créer un autre ticket
                  </Button>
                  <Button className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white shadow-lg">
                    Suivre mon ticket
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-green-50 to-teal-50 dark:from-emerald-950 dark:via-green-950 dark:to-teal-950">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-12">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-2xl mb-6 shadow-lg">
            <FileText className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-emerald-600 to-green-600 bg-clip-text text-transparent mb-3">
            Créer un nouveau ticket
          </h1>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Décrivez votre problème ou demande et notre équipe vous aidera dans les plus brefs délais
          </p>
        </div>

        <form onSubmit={handleSubmit} className="max-w-6xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Formulaire principal */}
            <div className="lg:col-span-2 space-y-8">
              <Card className="shadow-lg border-0 bg-white/70 dark:bg-gray-900/70 backdrop-blur-sm">
                <CardHeader className="pb-6">
                  <CardTitle className="flex items-center gap-3 text-xl">
                    <div className="p-2 bg-emerald-100 dark:bg-emerald-900/30 rounded-lg">
                      <MessageSquare className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    Informations du ticket
                  </CardTitle>
                  <CardDescription className="text-base">
                    Décrivez votre problème ou demande de manière détaillée
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="space-y-2">
                    <Label htmlFor="title" className="text-sm font-medium flex items-center gap-2">
                      <span>Titre du ticket</span>
                      <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="title"
                      value={formData.title}
                      onChange={(e) => handleInputChange("title", e.target.value)}
                      placeholder="Résumé concis de votre demande..."
                      className="h-12 border-emerald-200 focus:border-emerald-500 focus:ring-emerald-500 dark:border-emerald-800"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="description" className="text-sm font-medium flex items-center gap-2">
                      <span>Description détaillée</span>
                      <span className="text-red-500">*</span>
                    </Label>
                    <Textarea
                      id="description"
                      value={formData.description}
                      onChange={(e) => handleInputChange("description", e.target.value)}
                      placeholder="Décrivez votre problème ou demande en détail. Incluez les étapes pour reproduire le problème si applicable..."
                      className="min-h-[140px] border-emerald-200 focus:border-emerald-500 focus:ring-emerald-500 dark:border-emerald-800 resize-none"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="userEmail" className="text-sm font-medium flex items-center gap-2">
                      <User className="w-4 h-4" />
                      <span>Votre adresse email</span>
                      <span className="text-red-500">*</span>
                    </Label>
                    <Input
                      id="userEmail"
                      type="email"
                      value={formData.userEmail}
                      onChange={(e) => handleInputChange("userEmail", e.target.value)}
                      placeholder="votre.email@exemple.com"
                      className="h-12 border-emerald-200 focus:border-emerald-500 focus:ring-emerald-500 dark:border-emerald-800"
                    />
                  </div>
                </CardContent>
              </Card>

              <Card className="shadow-lg border-0 bg-white/70 dark:bg-gray-900/70 backdrop-blur-sm">
                <CardHeader className="pb-6">
                  <CardTitle className="flex items-center gap-3 text-xl">
                    <div className="p-2 bg-emerald-100 dark:bg-emerald-900/30 rounded-lg">
                      <Upload className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    Pièces jointes
                  </CardTitle>
                  <CardDescription className="text-base">
                    Ajoutez des captures d'écran ou documents pour nous aider à mieux comprendre
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="border-2 border-dashed border-emerald-300 dark:border-emerald-700 rounded-xl p-8 text-center bg-emerald-50/50 dark:bg-emerald-950/20 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 transition-colors">
                    <Upload className="mx-auto h-12 w-12 text-emerald-400 mb-4" />
                    <div className="space-y-2">
                      <Label htmlFor="file-upload" className="cursor-pointer">
                        <span className="text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 font-medium">
                          Cliquez pour télécharger
                        </span>
                        <span className="text-muted-foreground"> ou glissez-déposez vos fichiers</span>
                      </Label>
                      <Input
                        id="file-upload"
                        type="file"
                        multiple
                        onChange={handleFileUpload}
                        className="hidden"
                        accept=".jpg,.jpeg,.png,.pdf,.doc,.docx,.txt"
                      />
                      <p className="text-sm text-muted-foreground">PNG, JPG, PDF jusqu'à 10MB chacun</p>
                    </div>
                  </div>

                  {formData.attachments.length > 0 && (
                    <div className="mt-6 space-y-3">
                      <Label className="text-sm font-medium">Fichiers sélectionnés :</Label>
                      <div className="space-y-2">
                        {formData.attachments.map((file, index) => (
                          <div
                            key={index}
                            className="flex items-center justify-between bg-emerald-50 dark:bg-emerald-950/20 p-3 rounded-lg border border-emerald-200 dark:border-emerald-800"
                          >
                            <span className="text-sm font-medium text-emerald-700 dark:text-emerald-300">
                              {file.name}
                            </span>
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => removeAttachment(index)}
                              className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/20"
                            >
                              <X className="w-4 h-4" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Sidebar de catégorisation */}
            <div className="space-y-8">
              <Card className="shadow-lg border-0 bg-white/70 dark:bg-gray-900/70 backdrop-blur-sm">
                <CardHeader className="pb-6">
                  <CardTitle className="flex items-center gap-3 text-xl">
                    <div className="p-2 bg-emerald-100 dark:bg-emerald-900/30 rounded-lg">
                      <Tag className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    Catégorisation
                  </CardTitle>
                  <CardDescription className="text-base">
                    Aidez-nous à router votre ticket vers la bonne équipe
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="space-y-3">
                    <Label className="text-sm font-medium flex items-center gap-2">
                      <span>Type de ticket</span>
                      <span className="text-red-500">*</span>
                    </Label>
                    <Select value={formData.type} onValueChange={(value) => handleInputChange("type", value)}>
                      <SelectTrigger className="h-12 border-emerald-200 focus:border-emerald-500 focus:ring-emerald-500 dark:border-emerald-800">
                        <SelectValue placeholder="Sélectionner un type" />
                      </SelectTrigger>
                      <SelectContent>
                        {ticketTypes.map((type) => (
                          <SelectItem key={type.value} value={type.value}>
                            <div className="flex items-center gap-2">
                              <span>{type.icon}</span>
                              <span>{type.label}</span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {selectedType && (
                      <div className="flex items-center gap-2 p-2 bg-emerald-50 dark:bg-emerald-950/20 rounded-lg">
                        <span>{selectedType.icon}</span>
                        <span className="text-sm font-medium text-emerald-700 dark:text-emerald-300">
                          {selectedType.label}
                        </span>
                      </div>
                    )}
                  </div>

                  <div className="space-y-3">
                    <Label className="text-sm font-medium flex items-center gap-2">
                      <span>Catégorie</span>
                      <span className="text-red-500">*</span>
                    </Label>
                    <Select value={formData.category} onValueChange={(value) => handleInputChange("category", value)}>
                      <SelectTrigger className="h-12 border-emerald-200 focus:border-emerald-500 focus:ring-emerald-500 dark:border-emerald-800">
                        <SelectValue placeholder="Sélectionner une catégorie" />
                      </SelectTrigger>
                      <SelectContent>
                        {categories.map((category) => (
                          <SelectItem key={category.value} value={category.value}>
                            <div className="flex items-center gap-2">
                              <span>{category.icon}</span>
                              <span>{category.label}</span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {selectedCategory && (
                      <Badge className={`${selectedCategory.color} px-3 py-1`}>
                        <span className="mr-1">{selectedCategory.icon}</span>
                        {selectedCategory.label}
                      </Badge>
                    )}
                  </div>

                  <div className="space-y-3">
                    <Label className="text-sm font-medium flex items-center gap-2">
                      <span>Priorité</span>
                      <span className="text-red-500">*</span>
                    </Label>
                    <Select value={formData.priority} onValueChange={(value) => handleInputChange("priority", value)}>
                      <SelectTrigger className="h-12 border-emerald-200 focus:border-emerald-500 focus:ring-emerald-500 dark:border-emerald-800">
                        <SelectValue placeholder="Sélectionner une priorité" />
                      </SelectTrigger>
                      <SelectContent>
                        {priorities.map((priority) => (
                          <SelectItem key={priority.value} value={priority.value}>
                            <div className="flex items-center gap-2">
                              <span>{priority.icon}</span>
                              <span>{priority.label}</span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {selectedPriority && (
                      <Badge className={`${selectedPriority.color} px-3 py-1`}>
                        <span className="mr-1">{selectedPriority.icon}</span>
                        {selectedPriority.label}
                      </Badge>
                    )}
                  </div>
                </CardContent>
              </Card>

              <Card className="shadow-lg border-0 bg-white/70 dark:bg-gray-900/70 backdrop-blur-sm">
                <CardHeader className="pb-4">
                  <CardTitle className="flex items-center gap-3 text-lg">
                    <div className="p-2 bg-emerald-100 dark:bg-emerald-900/30 rounded-lg">
                      <Clock className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    Temps de réponse estimé
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {formData.priority === "urgent" && (
                    <Alert className="border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/20">
                      <AlertCircle className="h-4 w-4 text-red-600" />
                      <AlertDescription className="text-red-700 dark:text-red-300">
                        <strong>🔴 Urgent :</strong> Réponse sous 2h pendant les heures ouvrables
                      </AlertDescription>
                    </Alert>
                  )}
                  {formData.priority === "high" && (
                    <Alert className="border-orange-200 bg-orange-50 dark:border-orange-800 dark:bg-orange-950/20">
                      <AlertCircle className="h-4 w-4 text-orange-600" />
                      <AlertDescription className="text-orange-700 dark:text-orange-300">
                        <strong>🟠 Élevée :</strong> Réponse sous 4h pendant les heures ouvrables
                      </AlertDescription>
                    </Alert>
                  )}
                  {formData.priority === "medium" && (
                    <Alert className="border-amber-200 bg-amber-50 dark:border-amber-800 dark:bg-amber-950/20">
                      <AlertDescription className="text-amber-700 dark:text-amber-300">
                        <strong>🟡 Moyenne :</strong> Réponse sous 24h
                      </AlertDescription>
                    </Alert>
                  )}
                  {formData.priority === "low" && (
                    <Alert className="border-gray-200 bg-gray-50 dark:border-gray-700 dark:bg-gray-800">
                      <AlertDescription className="text-gray-700 dark:text-gray-300">
                        <strong>🟢 Faible :</strong> Réponse sous 72h
                      </AlertDescription>
                    </Alert>
                  )}
                  {!formData.priority && (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      Sélectionnez une priorité pour voir le temps de réponse estimé
                    </p>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>

          <Separator className="my-8 bg-emerald-200 dark:bg-emerald-800" />

          {/* Actions */}
          <div className="flex flex-col sm:flex-row justify-end gap-4">
            <Button
              type="button"
              variant="outline"
              className="border-emerald-300 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-700 dark:text-emerald-300 dark:hover:bg-emerald-950 h-12 px-8"
            >
              Annuler
            </Button>
            <Button
              type="submit"
              disabled={isSubmitting}
              className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white shadow-lg h-12 px-8 font-medium"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Création en cours...
                </>
              ) : (
                <>
                  <CheckCircle2 className="mr-2 h-4 w-4" />
                  Créer le ticket
                </>
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
