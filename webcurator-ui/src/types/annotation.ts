interface Annotation {
    date: Date,
    note: string,
    user: string,
    alert: boolean,
    type?: string
}

export type { Annotation }