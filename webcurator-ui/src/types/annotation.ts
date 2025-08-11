interface Annotation {
    date: number,
    note: string,
    user: string,
    alert: boolean,
    targetInstanceId?: string
}

export type { Annotation }