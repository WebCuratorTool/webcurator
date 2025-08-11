interface Annotation {
    date: string,
    note: string,
    user: string,
    alert: boolean,
    targetInstanceId?: string
}

export type { Annotation }