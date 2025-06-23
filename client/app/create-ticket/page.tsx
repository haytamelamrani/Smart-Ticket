"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

export default function CreateTicketPage() {
  const [form, setForm] = useState({
    title: "",
    description: "",
    category: "bug"
  })
  const [message, setMessage] = useState("")
  const [userName, setUserName] = useState("")
  const router = useRouter()

  useEffect(() => {
    const token = localStorage.getItem("token")
    const email = localStorage.getItem("email")
    if (!token || !email) {
      router.push("/login")
    } else {
      // 🔧 On pourrait ici fetch le nom depuis le backend via l'email si nécessaire
      const nameFromStorage = localStorage.getItem("name") || "Utilisateur connecté"
      setUserName(nameFromStorage)
    }
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleCategoryChange = (value: string) => {
    setForm({ ...form, category: value })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setMessage("")
    try {
      const res = await fetch("http://localhost:8080/api/tickets", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`
        },
        body: JSON.stringify(form)
      })

      const data = await res.json()
      if (!res.ok || data.error) {
        setMessage(data.error || "❌ Échec de création du ticket")
      } else {
        setMessage("✅ Ticket créé avec succès !")
        setForm({ title: "", description: "", category: "bug" })
      }
    } catch (err) {
      setMessage("❌ Erreur lors de la création du ticket")
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-xl w-full space-y-6">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-foreground">Créer un nouveau ticket</h2>
          <p className="text-muted-foreground mt-2">Bienvenue {userName} 👋</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Formulaire de signalement</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <Label htmlFor="title">Titre</Label>
                <Input
                  id="title"
                  name="title"
                  required
                  value={form.title}
                  onChange={handleChange}
                  placeholder="Problème d'accès..."
                />
              </div>
              <div>
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  name="description"
                  required
                  value={form.description}
                  onChange={handleChange}
                  placeholder="Décrivez le souci rencontré..."
                />
              </div>
              <div>
                <Label htmlFor="category">Catégorie</Label>
                <Select onValueChange={handleCategoryChange} defaultValue={form.category}>
                  <SelectTrigger>
                    <SelectValue placeholder="Choisir une catégorie" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="bug">🪲 Bug</SelectItem>
                    <SelectItem value="demande">📩 Demande</SelectItem>
                    <SelectItem value="suggestion">💡 Suggestion</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button type="submit" className="w-full bg-green-600 hover:bg-green-700 text-white">
                Envoyer le ticket
              </Button>
            </form>
            {message && <p className="mt-4 text-sm text-center text-muted-foreground">{message}</p>}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
